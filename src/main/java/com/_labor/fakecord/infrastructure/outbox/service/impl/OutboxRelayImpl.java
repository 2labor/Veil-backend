package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com._labor.fakecord.infrastructure.outbox.domain.EventStatus;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxTickEvent;
import com._labor.fakecord.infrastructure.outbox.repository.OutboxRepository;
import com._labor.fakecord.infrastructure.outbox.service.OutboxHandler;
import com._labor.fakecord.infrastructure.outbox.service.OutboxRelay;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class OutboxRelayImpl implements OutboxRelay {
  
  private final OutboxRepository repository;
  private final List<OutboxHandler> handlers;
  private final AtomicBoolean isProcessing = new AtomicBoolean(false); 

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onOutboxTick(OutboxTickEvent event) {
    log.debug("Messaage received: Outbox record created. Triggering relay.");
    internalProcess();
  }

  @Override
  @Transactional
  public void processNextBatch() {
    List<OutboxEvent> events = repository.findTopPending();

    if (events.isEmpty()) {
      return;
    }

    log.info("Relaying batch of {} events via handlers", events.size());

    for (OutboxEvent event : events) {
      processEvent(event);
    }
  }

  @Transactional
  private void internalProcess() {
    if (!isProcessing.compareAndSet(false, true)) {
      return;
    }

    try {
      List<OutboxEvent> events = repository.findTopPending();
      if (events.isEmpty()) return;

      log.info("Relaying batch of {} events via handlers", events.size());
      for (OutboxEvent event : events) {
        processEvent(event);
      }
    } finally {
      isProcessing.set(false);
    }
  }

  private void processEvent(OutboxEvent event) {
    List<OutboxHandler> supportedHandlers = handlers.stream()
      .filter(handler -> handler.supports(event.getType()))
      .toList();
      
    if (supportedHandlers.isEmpty()) {
      log.warn("No suitable handler found for event type: {}", event.getType());
      return;
    }

    boolean allSuccess = true;
    for (OutboxHandler handler : supportedHandlers) {
      try {
        handler.handle(event);
      } catch (Exception e) {
        log.error("Failed to process event {} with handler {}: {}", 
        event.getId(), handler.getClass().getSimpleName(), e.getMessage());
        allSuccess = false;
      }
    }

    if (allSuccess) {
      finalizeEvent(event);
    }
  }

  private void finalizeEvent(OutboxEvent event) {
    event.setStatus(EventStatus.PROCESS);
    event.setProcessAt(Instant.now());
    repository.save(event);
  }
}
