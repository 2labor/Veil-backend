package com._labor.fakecord.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.domain.enums.MessageType;
import com._labor.fakecord.domain.enums.SocketEventType;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com._labor.fakecord.infrastructure.outbox.domain.payload.MessageCreatedPayload;
import com._labor.fakecord.repository.ChannelRepository;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.MessageBroadcaster;
import com._labor.fakecord.services.MessageService;
import com._labor.fakecord.services.validation.ChannelAccessValidator;
import com._labor.fakecord.services.validation.MessageValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{

  private final MessageRepository repository;
  private final MessageBroadcaster broadcaster;
  private final ChannelRepository channelRepository;
  private final IdGenerator idGenerator;
  private final MessageValidator messageValidator;
  private final ChannelAccessValidator accessValidator;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  @Transactional
  public Message sendMessage(Long channelId, UUID authorId, String content, String nonce) {
    log.info("Sending message to channel {} by user {}", channelId, authorId);

     if (repository.existsByNonce(nonce)) {
      log.warn("Message with nonce {} already exists. Skipping.", nonce);
      return null;
    }

    accessValidator.accessValidation(channelId, authorId);
    messageValidator.validate(content);

    long messageId = idGenerator.nextId();
    Message message = Message.builder()
    .id(messageId)
    .type(MessageType.TEXT)
    .channelId(channelId)
    .authorId(authorId)
    .content(content)
    .nonce(nonce)
    .build();
    
    Message saved = repository.save(message);

    MessageCreatedPayload payload = new MessageCreatedPayload(
      saved.getId(),
      saved.getChannelId(),
      saved.getAuthorId()
    );
    kafkaTemplate.send("chat:messages", channelId.toString(), payload);
    broadcaster.broadcastMessageEvent(saved, SocketEventType.MESSAGE_CREATE);
    
    updateChannelMetadata(channelId, saved);
    return saved;
  }

  @Override
  @Transactional
  public Message editMessage(Long messageId, UUID operantId, String newContent) {
    messageValidator.validate(newContent);

    Message message = repository.findById(messageId)
      .orElseThrow(() -> new RuntimeException("MESSAGE_NOT_FOUND"));

    if (!message.getAuthorId().equals(operantId)) {
      throw new AccessDeniedException("NOT_THE_AUTHOR");
    }

    message.setContent(newContent);
    message.onUpdate();

    Message saved = repository.save(message);
    broadcaster.broadcastMessageEvent(message, SocketEventType.MESSAGE_UPDATE);

    log.debug("Message {} edited by author {}", messageId, operantId);

    return saved;
  }

  @Override
  @Transactional
  public Message sendSystemMessage(Long channelId, UUID authorId, MessageType type, String metadata) {
    log.info("Creating system message: type={}, channel={}, operator={}", type, channelId, authorId);

    String systemNonce = String.format("sys:%s:%d:%s", type, channelId, metadata);

    if (repository.existsByNonce(systemNonce)) {
      log.warn("System message with nonce {} already exists. Skipping.", systemNonce);
      return null;
    }

    Message systemMessage = Message.builder()
      .id(idGenerator.nextId())
      .channelId(channelId)
      .authorId(authorId)
      .type(type)
      .content(metadata)
      .nonce(systemNonce)
      .build();
    
    Message saved = repository.save(systemMessage);
    broadcaster.broadcastMessageEvent(saved, SocketEventType.MESSAGE_CREATE);

    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<Message> getLatestMessages(Long channelId, UUID currentUserId, int size) {
    accessValidator.accessValidation(channelId, currentUserId);

    log.debug("Fetching latest {} messages for channel {}", size, channelId);
    Pageable pageable = PageRequest.of(0, size);
    return repository.findAllByChannelIdOrderByIdDesc(channelId, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<Message> getMessagesBefore(Long channelId, UUID currentUserId, Long lastMessageId, int size) {
    accessValidator.accessValidation(channelId, currentUserId);

    log.debug("Fetching {} messages before ID {} for channel {}", size, lastMessageId, channelId);
    return repository.findAllByChannelIdAndIdLessThanOrderByIdDesc(channelId, lastMessageId, PageRequest.of(0, size));
  }

  @Override
  @Transactional
  public void deleteMessage(Long messageId, UUID requestId) {
    Message message = repository.findById(messageId)
      .orElseThrow(() -> new RuntimeException("Message not found"));

    if (!message.getAuthorId().equals(requestId)) {
      log.warn("User {} tried to delete message {} by user {}", requestId, messageId, message.getAuthorId());
      throw new RuntimeException("You can only delete your own messages");
    }

    repository.delete(message);
    broadcaster.broadcastDeletion(message.getChannelId(), messageId);

    log.info("Message {} deleted by author {}", messageId, requestId);
  }

  @Override
  @Transactional
  public void purgeChannelHistory(Long channelId, UUID requestId) {
    // TODO: Check for admin role
    log.info("Purging all messages in channel {} by request of {}", channelId, requestId);
    repository.deleteAllByChannelId(channelId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> getMessageContent(Long channelId, UUID currentUserId, Long targetMessageId, int limit) {
    accessValidator.accessValidation(channelId, currentUserId);

    log.info("Loading context window around message {} in channel {}", targetMessageId, channelId);
    int halfLimit = limit / 2;

    Message target = repository.findById(targetMessageId)
      .orElseThrow(() -> new RuntimeException("Target message not found"));

    List<Message> before = repository.findAllByChannelIdAndIdLessThanOrderByIdDesc(
      channelId, targetMessageId, PageRequest.of(0, halfLimit)
    ).getContent();

    List<Message> after = repository.findAllByChannelIdAndIdGreaterThanOrderByIdAsc(
      channelId, targetMessageId, PageRequest.of(0, halfLimit)
    ).getContent();

    List<Message> combined = new ArrayList<>();
    combined.addAll(before);
    combined.add(target);
    combined.addAll(after);

    combined.sort(Comparator.comparing(Message::getId));

    return combined;
  }  

  private void updateChannelMetadata(Long channelId, Message saved) {
    channelRepository.findById(channelId).ifPresent(channel -> {
      channel.setLastMessageId(saved.getId());
      channel.setLastActivityAt(Instant.now());

      if(channel.getType() == ChannelType.DM || channel.getType() == ChannelType.GROUP_DM) {
        String preview = saved.getContent();
        if (preview != null && preview.length() > 50) {
          preview = preview.substring(0, 48) + "...";
        }
        channel.setLastMessageContent(preview);
      }
      channelRepository.save(channel);
    });
  }
}
