package com._labor.fakecord.infrastructure.outbox.service.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.events.SocketEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatEventReceiver {
  private final SimpMessagingTemplate messagingTemplate;

  public void handleEvent(SocketEvent<?> event) {
    String destination = String.format("topic/channels.%s", event.getC());

    log.debug("Routing Redis event [{}] to WS: {}", event.getT(), destination);

    messagingTemplate.convertAndSend(destination, event);
  }
}