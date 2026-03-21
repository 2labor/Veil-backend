package com._labor.fakecord.services.impl;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.enums.MessageType;
import com._labor.fakecord.infrastructure.outbox.domain.ChannelMemberEventPayload;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.services.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelEventConsumer {
  private final MessageService messageService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "channel-events", groupId = "fakecord-message-group")
  @Transactional
  public void handleChannelEvent(String message) {
    try {
      JsonNode eventNode = objectMapper.readTree(message);
      String strType = eventNode.get("type").asText();
      OutboxEventType type = OutboxEventType.valueOf(strType);
      Long channelId = eventNode.get("aggregateId").asLong();

      String payloadRaw = eventNode.get("payload").asText();
      ChannelMemberEventPayload payload = objectMapper.readValue(
          payloadRaw, 
          ChannelMemberEventPayload.class
      );

      log.info("Processing event {} for channel {}", type, channelId);

      switch (type) {
        case CHANNEL_MEMBER_ADDED -> 
          messageService.sendSystemMessage(
            channelId, 
            payload.operatorId(),
            MessageType.RECIPIENT_ADD,
            payload.targetId().toString()
          );
        
        case CHANNEL_RENAMED -> 
          messageService.sendSystemMessage(
            channelId, 
            payload.operatorId(), 
            MessageType.CHANNEL_NAME_CHANGE, 
            payload.channelName()
        );

        case CHANNEL_MEMBER_REMOVED -> 
          messageService.sendSystemMessage(
            channelId, 
            payload.operatorId(), 
            MessageType.RECIPIENT_REMOVE, 
            payload.targetId().toString()
          );

        default -> log.debug("No system message required for type: {}", type);
      }
    } catch (Exception e) {  
      log.error("Critical error in ChannelEventConsumer: {}", e.getMessage(), e);
    }
  }
  
}
