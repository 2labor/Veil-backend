package com._labor.fakecord.infrastructure.outbox.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.service.EventPublisher;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RedisEventPublisher implements EventPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  private static final String TOPIC = "cache:evict";

  public RedisEventPublisher(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void publish(CacheEvictEvent event) {
    log.debug("Publishing evict event for aggregate: {} to topic: {}", event.aggregateId(), TOPIC);
    redisTemplate.convertAndSend(TOPIC, event);
  }
  
}
 