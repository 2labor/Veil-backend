package com._labor.fakecord.infrastructure.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.enums.ReactionAction;
import com._labor.fakecord.domain.events.ReactionEvent;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactionHotPathProcessor {
  
  private final RedisTemplate<String, Object> redisTemplate;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final IdGenerator idGenerator;
  private final ObjectMapper objectMapper;

  private final static String TOPIC = "chat.reactions";
  private final static String cacheKey = "reactions";

  public ReactionEvent toggleEvent(Long messageId, Long emojiId, UUID userId) {
    String userSetKey = String.format("%s:users:%d:%d", cacheKey, messageId, emojiId);
    String counterHashKey = String.format("%s:counters:%d", cacheKey, messageId);
    String userIdStr = userId.toString();
    String emojiFiled = String.valueOf(emojiId);

    ReactionAction action;

    Long removed = redisTemplate.opsForSet().remove(userSetKey, userIdStr);

    if (removed != null && removed > 0) {
      Long newCounter = redisTemplate.opsForHash().increment(counterHashKey, emojiFiled, -1);

      if (newCounter == null || newCounter <= 0) {
        redisTemplate.opsForHash().delete(counterHashKey, emojiFiled);
      }
      action = ReactionAction.REMOVE;
    } else {
      redisTemplate.opsForSet().add(userSetKey, userIdStr);
      redisTemplate.opsForHash().increment(counterHashKey, emojiFiled, 1);
      action = ReactionAction.ADD;
    }

    Long reactionId = idGenerator.nextId();
    ReactionEvent event = new ReactionEvent(reactionId, messageId, userId, emojiId, action);

    dispatchEvent(messageId, event);
    return event;
  }

  public void processClearAll(Long messageId) {
    String countersHashKey = String.format("%s:counters:%d", cacheKey, messageId);
    Set<Object> emojiFields = redisTemplate.opsForHash().keys(countersHashKey);

    if (emojiFields != null && !emojiFields.isEmpty()) {
      emojiFields.forEach(filed -> {
        String userSetKey = String.format("%s:users:%d:%d", cacheKey, messageId, filed);
        redisTemplate.delete(userSetKey);
      });
    }
    redisTemplate.delete(countersHashKey);

    ReactionEvent event = new ReactionEvent(null, messageId, null, null, ReactionAction.CLEAR_ALL);
    dispatchEvent(messageId, event);
  }

  public Map<Object, Object> getReactionCounters(Long messageId) {
    String counterHashKey = String.format("%s:counters:%d", cacheKey, messageId);
    return redisTemplate.opsForHash().entries(counterHashKey);
  }

  @SuppressWarnings("unchecked")
  public Map<Long, Map<Object, Object>> getReactionCountersForMessages(List<Long> messageIds) {
    if (messageIds == null || messageIds.isEmpty()) return Map.of();

    List<Object> rawResults = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
      messageIds.forEach(messageId -> {
        String counterHashKey = String.format("%s:counters:%d", cacheKey, messageId);
        connection.hashCommands().hGetAll(redisTemplate.getStringSerializer().serialize(counterHashKey));
      });
      return null;
    });

    Map<Long, Map<Object, Object>> bulkReactions = new HashMap<>();
    for (int i = 0; i < messageIds.size(); i++) {
      Long messageId = messageIds.get(i);
      Object resultNode = rawResults.get(i);

      if (resultNode instanceof Map) {
        bulkReactions.put(messageId, (Map<Object, Object>) resultNode);
      } else {
        bulkReactions.put(messageId, Map.of());
      }
    }
    return bulkReactions;
  }

  public boolean isUserReacted(Long messageId, Long emojiId, UUID userId) {
    String userSetKey = String.format("%s:users:%d:%d", cacheKey, messageId, emojiId);
    Boolean isMember = redisTemplate.opsForSet().isMember(userSetKey, userId.toString());
    return isMember != null && isMember;
  }

  private void dispatchEvent(Long messageId, ReactionEvent event) {
    try {
      String jsonPayload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(TOPIC, String.valueOf(messageId), jsonPayload);
    } catch (Exception e) {
      log.error("Failed to stream reaction event to Kafka for message {}", messageId, e);
      throw new RuntimeException("Infrastructure streaming error", e);
    }
  }
}
