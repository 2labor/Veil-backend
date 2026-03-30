package com._labor.fakecord.infrastructure.messaging;

import com._labor.fakecord.domain.events.SocketEvent;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;

public interface EventPublisher {
  void publish(CacheEvictEvent event);
  void publish(String topic, SocketEvent<?> event);
}
