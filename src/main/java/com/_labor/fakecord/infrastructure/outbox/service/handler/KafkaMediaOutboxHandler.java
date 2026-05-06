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
public class KafkaMediaOutboxHandler implements  OutboxHandler {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public boolean supports(OutboxEventType type) {
    return type == OutboxEventType.MEDIA_ATTACHMENT_READY;
  }

  @Override
  public void handle(OutboxEvent event) {
    log.info("Dispatching media processing task to Kafka for event: {}", event.getId());
    kafkaTemplate.send("media.processing", event.getAggregateId(), event.getPayload());
  }
  
}
