package com._labor.fakecord.infrastructure.websocket.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.events.UserStatusChangedEvent;
import com._labor.fakecord.services.PresenceBroadcastService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PresenceEventListener {

  private final PresenceBroadcastService broadcastService;

  @EventListener
  public void handleUserStatusChanged(UserStatusChangedEvent event) {
    broadcastService.broadcastStatusChange(event.userId(), event.newMask());
  }

}
