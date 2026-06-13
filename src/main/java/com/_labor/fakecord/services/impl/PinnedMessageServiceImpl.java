package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.entity.PinnedMessage;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.repository.PinnedMessageRepository;
import com._labor.fakecord.services.MessageService;
import com._labor.fakecord.services.PinnedMessageService;
import com._labor.fakecord.services.validation.ChannelAccessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PinnedMessageServiceImpl implements PinnedMessageService {

  private final PinnedMessageRepository repo;
  private final MessageRepository messageRepository;
  private final ChannelAccessValidator accessValidator;
  private final MessageService messageService;

  @Value("${app.pinned-messages.max-limit}")
  private int MAX_PINNED_MESSAGES;

  @Override
  @Transactional
  public void pinMessage(Long channelId, Long messageId, UUID operatorId) {
    log.info("User {} is pinning message {} in channel {}", operatorId, messageId, channelId);

    accessValidator.accessValidation(channelId, operatorId);

    if (repo.existsById(messageId)) {
      throw new IllegalArgumentException("MESSAGE_ALREADY_PINNED");
    }

    if (repo.countByChannelId(channelId) >= MAX_PINNED_MESSAGES) {
      throw new IllegalStateException("PINNED_MESSAGES_LIMIT_EXCEEDED");
    }

    Message message = messageRepository.findById(messageId)
    .orElseThrow(() -> new IllegalArgumentException("MESSAGE_NOT_FOUND"));
    if (!message.getChannelId().equals(channelId)) {
      throw new IllegalArgumentException("MESSAGE_DOES_NOT_BELONG_TO_CHANNEL");
    }

    PinnedMessage pinnedMessage = PinnedMessage.builder()
      .messageId(messageId)
      .channelId(channelId)
      .pinnedBy(operatorId)
      .build();
    repo.save(pinnedMessage);
  }

  @Override
  @Transactional
  public void unpinMessage(Long channelId, Long messageId, UUID operatorId) {
    log.info("User {} is unpinning message {} from channel {}", operatorId, messageId, channelId);
    accessValidator.accessValidation(channelId, operatorId);

    PinnedMessage pinnedMessage = repo.findById(messageId)
      .orElseThrow(() -> new IllegalArgumentException("PINNED_MESSAGE_NOT_FOUND"));
    
    if (!pinnedMessage.getChannelId().equals(channelId)) {
      throw new IllegalArgumentException("PINNED_MESSAGE_DOES_NOT_BELONG_TO_CHANNEL");
    }

    repo.delete(pinnedMessage);
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<MessageDto> getPinnedMessages(Long channelId, UUID currentUserId, int limit) {
    log.debug("Fetching pinned messages for channel {} by user {}", channelId, currentUserId);

    accessValidator.accessValidation(channelId, currentUserId);

    Slice<PinnedMessage> pinSlice = repo.findAllByChannelIdOrderByPinnedAtDesc(channelId, PageRequest.of(0, limit));
    List<Long> messageIds = pinSlice.getContent().stream()
      .map(PinnedMessage::getMessageId)
      .toList();

    List<Message> messages = messageRepository.findAllById(messageIds);
    Map<Long, Message> messageMap = messages.stream()
      .collect(Collectors.toMap(Message::getId, Function.identity()));

    List<Message> sortedMessages = messageIds.stream()
      .map(messageMap::get)
      .filter(msg -> msg != null)
      .toList();
    
    List<MessageDto> enrichedDtos = messageService.enrichMessagesBatch(sortedMessages);

    return pinSlice.map(pin -> enrichedDtos.stream()
      .filter(dto -> dto.id().equals(String.valueOf(pin.getMessageId())))
      .findFirst() 
      .orElse(null)
    );
  }

  @Override
  @Transactional(readOnly = true)
  public MessageDto getLatestPinnedMessage(Long channelId, UUID currentUserId) {
    accessValidator.accessValidation(channelId, currentUserId);

    PinnedMessage pinnedMessage = repo.findFirstByChannelIdOrderByPinnedAtDesc(channelId)
      .orElse(null);

    if (pinnedMessage == null) return null;

    Message message = messageRepository.findById(pinnedMessage.getMessageId())
      .orElseThrow(() -> new IllegalArgumentException("MESSAGE_NOT_FOUND"));

    return messageService.enrichMessagesBatch(List.of(message)).get(0);
  }

  @Override
  @Transactional(readOnly = true)
  public MessageDto getNextPinnedMessage(Long channelId, Long currentMessageId, UUID currentUserId, String direction) {
    accessValidator.accessValidation(channelId, currentUserId);

    PinnedMessage pinnedMessage = repo.findById(currentMessageId)
      .orElseThrow(() -> new IllegalArgumentException("CURRENT_PIN_NOT_FOUND"));

    Optional<PinnedMessage> targetPin;
    if ("PREVIOUS".equalsIgnoreCase(direction)) {
      targetPin = repo.findFirstByChannelIdAndPinnedAtLessThanOrderByPinnedAtDesc(channelId, pinnedMessage.getPinnedAt());
      if (targetPin.isEmpty()) {
        targetPin = repo.findFirstByChannelIdOrderByPinnedAtDesc(channelId);
      }
    } else {
      targetPin = repo.findFirstByChannelIdAndPinnedAtGreaterThanOrderByPinnedAtAsc(channelId, pinnedMessage.getPinnedAt());
      if (targetPin.isEmpty()) {
        return null;
      }
    }
    if (targetPin.isEmpty()) {
      return null;
    }

    Message targetMessage = messageRepository.findById(targetPin.get().getMessageId())
      .orElseThrow(() -> new IllegalArgumentException("TARGET_MESSAGE_NOT_FOUND"));

    return messageService.enrichMessagesBatch(List.of(targetMessage)).get(0);
  }
}