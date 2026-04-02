package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
  
import com._labor.fakecord.infrastructure.outbox.domain.EventStatus;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxTickEvent;
import com._labor.fakecord.infrastructure.outbox.repository.OutboxRepository;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OutboxServiceImpl implements OutboxService {

  private final OutboxRepository repository;
  private final ApplicationEventPublisher publisher;
  private final ObjectMapper mapper;

  public OutboxServiceImpl(OutboxRepository repository, ObjectMapper mapper, ApplicationEventPublisher publisher) {
    this.repository = repository;
    this.mapper = mapper;
    this.publisher = publisher;
  }

  @Override
  public void publish(UUID aggregateId, OutboxEventType type, Object payload) {
    try {
      String jsonPayload = mapper.writeValueAsString(payload);

      OutboxEvent event = OutboxEvent.builder()
        .type(type)
        .aggregateId(aggregateId.toString())
        .payload(jsonPayload)
        .status(EventStatus.PENDING)
        .retryCount(0)
        .build();
      
        repository.save(event); 

        publisher.publishEvent(new OutboxTickEvent());

        log.info("Event {} for aggregate {} saved to outbox", type, aggregateId);
    } catch (Exception e) {
      log.error("Failed to map event payload to JSON for aggregate {}", aggregateId, e);
      throw new RuntimeException("Event serialization failed", e);
    }
  }
  
}
