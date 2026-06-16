package com._labor.fakecord.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.MessageReactionsResponse;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.events.ReactionEvent;
import com._labor.fakecord.infrastructure.cache.ReactionHotPathProcessor;
import com._labor.fakecord.repository.MessageReactionRepository;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.ReactionService;
import com._labor.fakecord.services.validation.ChannelAccessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionServiceImpl implements ReactionService {

  // private final MessageReactionRepository repo;
  private final ChannelAccessValidator accessValidator;
  private final MessageRepository messageRepository;
  private final ReactionHotPathProcessor hotProcessor;

  @Override
  public ReactionEvent toggleReaction(Long messageId, Long emojiId, UUID userId) {
    Message message = messageRepository.findById(messageId).orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + messageId));

    accessValidator.accessValidation(message.getChannelId(), userId);
    return hotProcessor.toggleEvent(messageId, emojiId, userId);
  }

  @Override
  public void clearAllReactions(Long messageId, UUID operatorId) {
    Message message = messageRepository.findById(messageId)
      .orElseThrow(() -> new IllegalArgumentException("Message not found with id: " + messageId));

    accessValidator.accessValidation(message.getChannelId(), operatorId);

    if (!message.getAuthorId().equals(operatorId)) {
      log.warn("User {} tried to clear reactions for message {} without rights", operatorId, messageId);
      throw new AccessDeniedException("You are not allowed to clear reactions for this message");
    }
    hotProcessor.processClearAll(messageId);
    log.info("Successfully triggered clear_all for message {} by operator {}", messageId, operatorId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<MessageReactionsResponse> getAllReactionsForMessage(Long messageId, UUID currentUserId) {
    Map<Object, Object> counters = hotProcessor.getReactionCounters(messageId);

    List<MessageReactionsResponse> response = new ArrayList<>();
    counters.forEach((emojiKey, emojiCounter) -> {
      Long emojiId = Long.valueOf(emojiKey.toString());
      Long counter = Long.valueOf(emojiCounter.toString());

      boolean selfReacted = hotProcessor.isUserReacted(messageId, emojiId, currentUserId);

      response.add(new MessageReactionsResponse(emojiId, counter, selfReacted));
    });

    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public Map<Long, List<MessageReactionsResponse>> getReactionsToMessages(List<Long> messageIds, UUID currentUserId) {
    if (messageIds == null || messageIds.isEmpty()) return Map.of();

    Map<Long, Map<Object, Object>> bulkCounter = hotProcessor.getReactionCountersForMessages(messageIds);
    Map<Long, List<MessageReactionsResponse>> finalResult = new HashMap<>();

    bulkCounter.forEach((messageId, counterMap) -> {
      List<MessageReactionsResponse> messageReactions = new ArrayList<>();

      counterMap.forEach((emojiIdKey, countVal) -> {
        Long emojiId = Long.valueOf(emojiIdKey.toString());
        Long count = Long.valueOf(countVal.toString());

        boolean selfReacted = hotProcessor.isUserReacted(messageId, emojiId, currentUserId);
        messageReactions.add(new MessageReactionsResponse(emojiId, count, selfReacted));
      });

      finalResult.put(messageId, messageReactions);
    });
    return finalResult;
  }

  // @Override
  // @Transactional(readOnly = true)
  // public Slice<UserProfileShort> getUsersForReactionSlice(Long messageId, Long emojiId, Pageable pageable) {
  //   Slice<MessageReaction> reactionSlice = repo.findByMessageIdAndEmojiId(messageId, emojiId, pageable);
    
  //   List<UUID> userIds = reactionSlice.getContent().stream()
  //     .map(MessageReaction::getUserId)
  //     .collect(Collectors.toList());

  //   List<UserProfileShort> userProfiles = userProfileCache.getUserProfile(userIds).
  // }
  
}
