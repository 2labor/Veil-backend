package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.notifications.NotificationPayload;
import com._labor.fakecord.domain.notifications.SystemNotification;
import com._labor.fakecord.services.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisNotificationService implements NotificationService {
  
  private final RedisTemplate<String, Object> redisTemplate;
  
  private static final String USER_PREFIX = "users:notifications:";
  private static final String CHANNEL_PREFIX = "channel:events:";

  @Override
  public <T extends NotificationPayload> void sendToUser(UUID userId, SystemNotification<T> notification) {
    publish(USER_PREFIX + userId, notification); 
  }

  @Override
  public <T extends NotificationPayload> void sendToChannel(Long channelId, SystemNotification<T> notification) {
    publish(CHANNEL_PREFIX + channelId, notification);
  }

  @Override
  public <T extends NotificationPayload> void broadcastPresence(Long channelId, SystemNotification<T> notification) {
    publish(CHANNEL_PREFIX + channelId, notification);
  }

  private void publish(String topic, Object message) {
    try {
      redisTemplate.convertAndSend(topic, message);
    } catch (Exception e) {
      log.error("Failed to publish to Redis topic {}: {}", topic, e.getMessage());
    }
  }
}
