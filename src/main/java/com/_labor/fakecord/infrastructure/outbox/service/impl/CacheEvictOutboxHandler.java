package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.RelationshipActionPayload;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheSubType;
import com._labor.fakecord.infrastructure.outbox.service.OutboxHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheEvictOutboxHandler implements OutboxHandler {

  private final RedisEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;
  private static final Set<OutboxEventType> EVICT_TYPES = Set.of(
    OutboxEventType.SOCIAL_FRIEND_REQUEST_SENT,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_DECLINED,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_ACCEPTED,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_CANCELLED,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_IGNORED,
    OutboxEventType.SOCIAL_FRIENDSHIP_CREATED,
    OutboxEventType.SOCIAL_FRIENDSHIP_TERMINATED
  ); 

  @Override
  public boolean supports(OutboxEventType type) {
    return EVICT_TYPES.contains(type);
  }
  
  @Override
  public void handle(OutboxEvent event) {
    log.info("Processing outbox event type: {} for aggregate: {}", event.getType(), event.getAggregateId());

    try {
      RelationshipActionPayload payload = objectMapper.readValue(
        event.getPayload(), RelationshipActionPayload.class
      );

      
      UUID actorId  = payload.actorId();
      UUID targetId  = payload.targetId();

      log.debug("Evicting caches for actor: {} and target: {}", actorId, targetId);


      evictAllCaches(actorId);
      evictAllCaches(targetId);

      log.info("Successfully published eviction events for friendship action: {}", event.getType());
      
    } catch (Exception e) {
        log.error("Error processing outbox event {}: {}", event.getId(), e.getMessage(), e);
    }
  }

  private void evictAllCaches(UUID userId) {
    eventPublisher.publish(new CacheEvictEvent(userId, "all", CacheSubType.ALL, System.currentTimeMillis()));
  }
}
