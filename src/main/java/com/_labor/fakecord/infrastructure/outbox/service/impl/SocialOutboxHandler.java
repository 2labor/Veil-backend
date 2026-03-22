package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.Set;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.entity.FriendRequest;
import com._labor.fakecord.infrastructure.outbox.domain.FriendAcceptedPayload;
import com._labor.fakecord.infrastructure.outbox.domain.FriendRequestPayload;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.RelationshipActionPayload;
import com._labor.fakecord.infrastructure.outbox.domain.UserBlockPayload;
import com._labor.fakecord.infrastructure.outbox.service.OutboxHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocialOutboxHandler implements OutboxHandler{

  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final ObjectMapper objectMapper;
  
  private static final String SOCIAL_TOPIC = "social-events";

  private static final Set<OutboxEventType> SUPPORTED = Set.of(
    OutboxEventType.SOCIAL_FRIEND_REQUEST_SENT,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_ACCEPTED,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_DECLINED,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_CANCELLED,
    OutboxEventType.SOCIAL_FRIEND_REQUEST_IGNORED,
    OutboxEventType.SOCIAL_FRIENDSHIP_CREATED,
    OutboxEventType.SOCIAL_FRIENDSHIP_TERMINATED,
    OutboxEventType.SOCIAL_USER_BLOCKED,
    OutboxEventType.SOCIAL_USER_UNBLOCKED
  );

  @Override
  public boolean supports(OutboxEventType type) {
    return SUPPORTED.contains(type);
  }

  @Override
  public void handle(OutboxEvent event) {
    try {
      Object payload = resolvePayload(event);

      kafkaTemplate.send(SOCIAL_TOPIC, event.getAggregateId().toString(), payload);

      log.info("Social event {} relayed to Kafka", event.getType());
    } catch (Exception e) {
      log.error("Relay failed for event {}: {}", event.getId(), e.getMessage());
      throw new RuntimeException("Relay failure", e);
    }
  }
  
  private Object resolvePayload(OutboxEvent event) throws Exception {
    return switch(event.getType()) {
      case SOCIAL_FRIEND_REQUEST_SENT -> 
        objectMapper.readValue(event.getPayload(), FriendRequestPayload.class);
      case SOCIAL_FRIEND_REQUEST_ACCEPTED, SOCIAL_FRIENDSHIP_CREATED -> 
        objectMapper.readValue(event.getPayload(), FriendAcceptedPayload.class);
      case SOCIAL_USER_BLOCKED -> 
        objectMapper.readValue(event.getPayload(), UserBlockPayload.class);
      case SOCIAL_FRIEND_REQUEST_DECLINED, 
        SOCIAL_FRIEND_REQUEST_CANCELLED, 
        SOCIAL_FRIEND_REQUEST_IGNORED, 
        SOCIAL_FRIENDSHIP_TERMINATED, 
        SOCIAL_USER_UNBLOCKED -> 
        objectMapper.readValue(event.getPayload(), RelationshipActionPayload.class);
      default -> 
        throw new IllegalArgumentException("Unsupported event type!");
    };
  }
}
