package com._labor.fakecord.infrastructure.outbox.service.impl;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.SocketEventType;
import com._labor.fakecord.domain.events.SocketEvent;
import com._labor.fakecord.domain.events.ReactionEvent;
import com._labor.fakecord.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactionNotificationWorker {
  
  private final ObjectMapper objectMapper;
  private final MessageRepository messageRepository;
  private final RedisTemplate<String, Object> redisTemplate;

  @KafkaListener(topics = "chat.reactions", groupId = "fakecord-reaction-indexer-group")
  public void broadcastReaction(String rawPayload) {
    try {
        String finalJson = rawPayload;
        if (rawPayload.startsWith("\"") && rawPayload.endsWith("\"")) {
          finalJson = objectMapper.readValue(rawPayload, String.class);
        }

        ReactionEvent event = objectMapper.readValue(finalJson, ReactionEvent.class);

        Message message = messageRepository.findById(event.messageId()).orElse(null);
        if (message == null) {
          log.warn("Notification Worker: Message {} not found, skipping broadcast", event.messageId());
          return;
        }

        Long channelId = message.getChannelId();

        SocketEvent<ReactionEvent> socketEvent = SocketEvent.of(
            SocketEventType.MESSAGE_REACTION, 
            channelId, 
            event
        );

        String redisPubSubTopic = "channel:events:" + channelId;
        redisTemplate.convertAndSend(redisPubSubTopic, socketEvent);
        
        log.debug("Notification Worker: Broadcasted SocketEvent for reaction on message {} to channel {}", event.messageId(), channelId);
    } catch (Exception e) {
      log.error("Error broadcasting reaction event", e);
    }
  }

}