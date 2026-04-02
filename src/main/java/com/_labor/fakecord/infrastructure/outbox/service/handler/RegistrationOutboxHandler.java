package com._labor.fakecord.infrastructure.outbox.service.handler;

import java.util.Set;

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
public class RegistrationOutboxHandler implements OutboxHandler {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private static final String TOPIC = "user-registration-events";

  private static final Set<OutboxEventType> SUPPORTED_EVENT_TYPES = Set.of(
    OutboxEventType.USER_REGISTERED,
    OutboxEventType.USER_EMAIL_VERIFIED,
    OutboxEventType.USER_DELETED
  ); 

  @Override
  public boolean supports(OutboxEventType type) {
    return SUPPORTED_EVENT_TYPES.contains(type);
  }

  @Override
  public void handle(OutboxEvent event) {
    try {
      kafkaTemplate.send(TOPIC, event.getAggregateId().toString(), event.getPayload().toString());
      log.debug("Registration event {} sent to Kafka", event.getId());
    } catch (Exception e) {
      throw new RuntimeException("Failed to relay registration event to Kafka", e);
    }
  }
  
}
