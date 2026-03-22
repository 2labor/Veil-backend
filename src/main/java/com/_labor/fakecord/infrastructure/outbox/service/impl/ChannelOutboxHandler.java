package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.Set;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.ChannelMemberEventPayload;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.service.OutboxHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChannelOutboxHandler implements OutboxHandler {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;

  private static final String TOPIC = "channel-events";

  private static final Set<OutboxEventType> SUPPORTED_TYPES = Set.of(
    OutboxEventType.CHANNEL_CREATED,
    OutboxEventType.CHANNEL_MEMBER_ADDED,
    OutboxEventType.CHANNEL_MEMBER_REMOVED,
    OutboxEventType.CHANNEL_RENAMED
  );

  @Override
  public boolean supports(OutboxEventType type) {
    return SUPPORTED_TYPES.contains(type);
  }

  @Override
  public void handle(OutboxEvent event) {
    try {
      ChannelMemberEventPayload payload = objectMapper.readValue(
        event.getPayload(),ChannelMemberEventPayload.class
      );

      kafkaTemplate.send(TOPIC, event.getAggregateId().toString(), payload);
      log.info("Channel event {} for channel {} relayed to Kafka", event.getType(), event.getAggregateId());
    } catch (Exception e) {
      log.error("Failed to relay channel outbox event {}: {}", event.getId(), e.getMessage());
      throw new RuntimeException("Failed to relay channel event", e);
    }
  }
  
}
