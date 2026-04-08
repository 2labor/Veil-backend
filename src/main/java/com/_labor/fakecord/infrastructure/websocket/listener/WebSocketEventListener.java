package com._labor.fakecord.infrastructure.websocket.listener;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com._labor.fakecord.infrastructure.presence.PresenceService;
import com._labor.fakecord.services.UserStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
  
  private final UserStatusService statusService;
  private final PresenceService presenceService;

  @EventListener
  public void handleWebSocketConnectListener(SessionConnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    UUID userId = UUID.fromString(getUserId(accessor)); 

    if (userId != null) {
      log.debug("WebSocket Session Connect: user {}", userId);
      statusService.setOnline(userId); 
    }
  }

  @EventListener
  public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    UUID userId = UUID.fromString(getUserId(accessor));

    if (userId != null) {
      log.debug("WebSocket Session Disconnect: user {}", userId);
      
      statusService.setOffline(userId);
      presenceService.leaveChannel(userId);
    }
  }

  private String getUserId(StompHeaderAccessor accessor) {
    if (null == accessor.getSessionAttributes()) return null;
    return (String) accessor.getSessionAttributes().get("userId");
  }

}
