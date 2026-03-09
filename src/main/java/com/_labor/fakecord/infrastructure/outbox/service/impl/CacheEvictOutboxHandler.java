package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.RelationshipActionPayload;
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
    OutboxEventType.SOCIAL_FRIENDSHIP_CREATED,
    OutboxEventType.SOCIAL_FRIENDSHIP_TERMINATED,
    OutboxEventType.SOCIAL_USER_BLOCKED,
    OutboxEventType.SOCIAL_USER_UNBLOCKED
  ); 

  @Override
  public boolean supports(OutboxEventType type) {
    return EVICT_TYPES.contains(type);
  }

  @Override
public void handle(OutboxEvent event) {
    eventPublisher.publish(new CacheEvictEvent(
        event.getAggregateId(), "friends", System.currentTimeMillis()
    ));

    try {
        RelationshipActionPayload payload = objectMapper.readValue(
            event.getPayload(), RelationshipActionPayload.class
        );
        UUID other = payload.targetId().equals(event.getAggregateId())
            ? payload.actorId()
            : payload.targetId();
        eventPublisher.publish(new CacheEvictEvent(
            other, "friends", System.currentTimeMillis()
        ));
    } catch (Exception e) {
        log.warn("Could not parse payload for dual evict: {}", e.getMessage());
    }
  }

}
