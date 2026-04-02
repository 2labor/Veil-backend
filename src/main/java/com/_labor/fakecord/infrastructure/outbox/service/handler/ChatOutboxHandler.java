package com._labor.fakecord.infrastructure.outbox.service.handler;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.service.OutboxHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatOutboxHandler implements OutboxHandler {

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private static final String CHAT_TOPIC = "chat-events";

  @Override
  public boolean supports(OutboxEventType type) {
    return type.name().startsWith("MESSAGE_") || type.name().startsWith("CHANNEL_");
  }

  @Override
  public void handle(OutboxEvent event) {
    kafkaTemplate.send(CHAT_TOPIC, event.getAggregateId(), event.getPayload());
    log.info("Chat event {} (Channel: {}) relayed to Kafka", event.getType(), event.getAggregateId());
  }
  
}
