package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.RelationshipActionPayload;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheSubType;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
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

  @Override
  public boolean supports(OutboxEventType type) {
    return type.name().startsWith("SOCIAL_") ||type == OutboxEventType.USER_PROFILE_UPDATED;
  }

  @Override
  public void handle(OutboxEvent event) {
    log.info("Processing eviction for event type: {} and aggregate: {}", event.getType(), event.getAggregateId());

    try {
      if (event.getType() == OutboxEventType.USER_PROFILE_UPDATED) {
        publish(event.getAggregateId(), CacheType.USER_PROFILES, CacheSubType.ALL);
        return;
      }

      RelationshipActionPayload payload = objectMapper.readValue(
        event.getPayload(), 
        RelationshipActionPayload.class
      );

      handleSocialEvent(event.getType(), payload);
    } catch (Exception e) {
      log.error("Failed to process outbox event {}: {}", event.getId(), e.getMessage(), e);
    }
  }
 
  private void handleSocialEvent(OutboxEventType type , RelationshipActionPayload payload) {
    UUID actorId = payload.actorId();
    UUID targetId = payload.targetId();

    switch (type) {
      case SOCIAL_FRIEND_REQUEST_SENT -> {
        publish(actorId, CacheType.REQUESTS_ONGOING, CacheSubType.OUTGOING_LIST);
        publish(actorId, CacheType.REQUEST_COUNTER, CacheSubType.OUTGOING_COUNTER);
        
        publish(targetId, CacheType.REQUESTS_INCOMING, CacheSubType.INCOMING_LIST);
        publish(targetId, CacheType.REQUEST_COUNTER, CacheSubType.INCOMING_COUNTER);
      }

      case SOCIAL_FRIEND_REQUEST_ACCEPTED, SOCIAL_FRIENDSHIP_CREATED, SOCIAL_FRIENDSHIP_TERMINATED -> {
        publish(actorId, CacheType.ALL, CacheSubType.ALL);
        publish(targetId, CacheType.ALL, CacheSubType.ALL);
      }

      case SOCIAL_FRIEND_REQUEST_DECLINED, SOCIAL_FRIEND_REQUEST_CANCELLED, SOCIAL_FRIEND_REQUEST_IGNORED-> {
        publish(actorId, CacheType.REQUESTS_ONGOING, CacheSubType.OUTGOING_LIST);
        publish(actorId, CacheType.REQUEST_COUNTER, CacheSubType.OUTGOING_COUNTER);
        publish(targetId, CacheType.REQUESTS_INCOMING, CacheSubType.INCOMING_LIST);
        publish(targetId, CacheType.REQUEST_COUNTER, CacheSubType.INCOMING_COUNTER);
      }

      case SOCIAL_USER_BLOCKED -> {
        publish(actorId, CacheType.ALL, CacheSubType.ALL);
        publish(actorId, CacheType.BLOCKS, CacheSubType.BLOCK_LIST);
        publish(targetId, CacheType.ALL, CacheSubType.ALL);
      }

      case SOCIAL_USER_UNBLOCKED -> {
        publish(actorId, CacheType.BLOCKS, CacheSubType.BLOCK_LIST);
      }

      default -> log.debug("Event {} handled by default logic", type);
    }
  } 

  private void publish(UUID userId, CacheType type, CacheSubType subType) {
    eventPublisher.publish(new CacheEvictEvent(
      userId, 
      type.getName(), 
      subType,
      System.currentTimeMillis()
    ));
  }
  
}
