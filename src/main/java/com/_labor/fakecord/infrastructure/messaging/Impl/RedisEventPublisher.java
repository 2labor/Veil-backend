package com._labor.fakecord.infrastructure.messaging.Impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.events.SocketEvent;
import com._labor.fakecord.infrastructure.messaging.EventPublisher;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;

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

  @Override
  public void publish(String topic, SocketEvent<?> event) {
    log.debug("Publishing event to Redis topic [{}]: {}", topic, event);
    redisTemplate.convertAndSend(topic, event);
  }
  
}
 