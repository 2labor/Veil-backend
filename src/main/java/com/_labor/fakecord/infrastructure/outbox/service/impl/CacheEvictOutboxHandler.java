package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.Set;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.service.OutboxHandler;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CacheEvictOutboxHandler implements OutboxHandler {

  private final RedisEventPublisher eventPublisher;
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
    CacheEvictEvent cacheEvent = new CacheEvictEvent(
      event.getAggregateId(),
      "friends",
      System.currentTimeMillis()
    );

    eventPublisher.publish(cacheEvent);
  }

}
