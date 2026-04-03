package com._labor.fakecord.infrastructure.notification.Impl;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.notification.EventReceiver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisNotificationReceiver implements EventReceiver, MessageListener {

  private final SimpMessagingTemplate messageTemplate;
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void handleEvent(String topic, Object body) {
    log.debug("Routing event from topic: {}", topic);

    String[] parts = topic.split(":");
    if (parts.length < 1) return;

    String category = parts[0];
    switch(category) {
      case "users" -> {
        String userId = parts[parts.length - 1];
        log.info("convertAndSendToUser userId={}", userId);
        messageTemplate.convertAndSendToUser(userId, "/queue/notifications", body);
        log.info("convertAndSendToUser DONE");
      }
      case "channel" -> {
        String channelId = parts[parts.length - 1];
        messageTemplate.convertAndSend("/topic/channel." + channelId, body);
      } 
      case "cache" -> log.trace("Handling cache evict: {}", body);
      default -> log.warn("No route for category: {}", category);
    }
  }

    @Override
    public void onMessage(Message message, byte[] pattern) {
      try {
        @SuppressWarnings("unchecked")
        RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
        
        String topic = new String(message.getChannel());
        log.info("RECEIVED REDIS MESSAGE on topic: {}", topic);
        Object body = valueSerializer.deserialize(message.getBody());

        log.info("Deserialized body type: {}", body.getClass().getName()); 
        log.info("Body content: {}", body); 

        handleEvent(topic, body);
        } catch (Exception e) {
          log.error("Error processing Redis message: {}", e.getMessage());
        }
    }
  
}
