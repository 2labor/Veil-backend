package com._labor.fakecord.infrastructure.outbox.service;

import java.util.UUID;

import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;

public interface OutboxService {
  void publish(UUID aggregateId, OutboxEventType type, Object payload);
}