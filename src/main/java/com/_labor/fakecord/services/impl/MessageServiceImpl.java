package com._labor.fakecord.services.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.MessageType;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com._labor.fakecord.repository.ChannelMemberRepository;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService{

  private final MessageRepository repository;
  private final ChannelMemberRepository memberRepository;
  private final IdGenerator idGenerator;

  @Override
  public Message sendMessage(Long channelId, UUID authorId, String content, String nonce) {
    log.info("Sending message to channel {} by user {}", channelId, authorId);

    if (!memberRepository.existsById_ChannelIdAndId_UserId(channelId, authorId)) {
      log.warn("Access denied for user {} in channel {}", authorId, channelId);
      throw new RuntimeException("You are not a member of this channel");
    }

    long messageId = idGenerator.nextId();

    Message message = Message.builder()
    .id(messageId)
    .type(MessageType.TEXT)
    .channelId(channelId)
    .authorId(authorId)
    .content(content)
    .nonce(nonce)
    .build();

    return repository.save(message);
  }

  
  @Override
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
      .channelId(channelId)
      .authorId(authorId)
      .type(type)
      .content(metadata)
      .nonce(systemNonce)
      .build();
    
    return repository.save(systemMessage);
  }

  @Override
  public Slice<Message> getLatestMessages(Long channelId, int size) {
    log.debug("Fetching latest {} messages for channel {}", size, channelId);
    Pageable pageable = PageRequest.of(0, size);
    return repository.findAllByChannelIdOrderByIdDesc(channelId, pageable);
  }

  @Override
  public Slice<Message> getMessagesBefore(Long channelId, Long lastMessageId, int size) {
    log.debug("Fetching {} messages before ID {} for channel {}", size, lastMessageId, channelId);
    return repository.findAllByChannelIdAndIdLessThanOrderByIdDesc(channelId, lastMessageId, PageRequest.of(0, size));
  }

  @Override
  public void deleteMessage(Long messageId, UUID requestId) {
    Message message = repository.findById(messageId)
      .orElseThrow(() -> new RuntimeException("Message not found"));

    if (!message.getAuthorId().equals(requestId)) {
      log.warn("User {} tried to delete message {} by user {}", requestId, messageId, message.getAuthorId());
      throw new RuntimeException("You can only delete your own messages");
    }

    repository.delete(message);
    log.info("Message {} deleted by author {}", messageId, requestId);
  }

  @Override
  public void purgeChannelHistory(Long channelId, UUID requestId) {
    // TODO: Check for admin role
    log.info("Purging all messages in channel {} by request of {}", channelId, requestId);
    repository.deleteAllByChannelId(channelId);
  }

  @Override
  public List<Message> getMessageContent(Long channelId, Long targetMessageId, int limit) {
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
}
