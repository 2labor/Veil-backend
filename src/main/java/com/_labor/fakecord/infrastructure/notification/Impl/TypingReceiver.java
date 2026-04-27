package com._labor.fakecord.infrastructure.notification.Impl;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.events.TypingEvent;
import com._labor.fakecord.infrastructure.notification.EventReceiver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TypingReceiver implements MessageListener, EventReceiver {

  private final SimpMessagingTemplate messageTemplate;
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public void onMessage(Message message, byte[] pattern) {
    try {
      String topic = new String(message.getChannel());

      RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
      Object body = valueSerializer.deserialize(message.getBody());

      log.trace("Received redis message on topic: {}", topic);
      handleEvent(topic, body);
    } catch (Exception e) {
      log.error("Error processing Typing Redis message: {}", e.getMessage());
    }
  }

  @Override
  public void handleEvent(String topic, Object body) {
    if (body instanceof TypingEvent event) {
      log.trace("Forwarding typing event to WS: channel={}, user={}", event.channelId(), event.userName());
      
      messageTemplate.convertAndSend(
        "/topic/channel." + event.channelId() + ".typing", 
        event
      );
    } else {
      log.warn("TypingReceiver received unexpected body type: {}", 
      body != null ? body.getClass().getSimpleName() : "null");
    }
  }
}
