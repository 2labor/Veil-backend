package com._labor.fakecord.interceptor;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.UUID;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com._labor.fakecord.services.UserStatusService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PresenceInterceptor implements ChannelInterceptor {

  private final UserStatusService statusService;

  public PresenceInterceptor(UserStatusService statusService) {
    this.statusService = statusService;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || accessor.getCommand() == null) return message;

    StompCommand cmd = accessor.getCommand();

    String userId = getUserId(accessor);

    if (userId != null) {
      switch (cmd) {
        case CONNECT -> {
          statusService.setOnline(UUID.fromString(userId));
          log.debug("Presence ONLINE: user={} cmd={}", userId, cmd);
        }
        default -> {}
      }
    } else {
      if (cmd == StompCommand.CONNECT && accessor.getUser() != null) {
        String principalName = accessor.getUser().getName();
        if (principalName != null && !principalName.isBlank()) {
          try {
            UUID.fromString(principalName);
            statusService.setOnline(UUID.fromString(principalName));
            log.debug("Presence ONLINE via Principal: user={}", principalName);
          } catch (IllegalArgumentException e) {
            log.warn("Presence: principal name is not a UUID: {}", principalName);
          }
        }
      } else {
        log.trace("Presence: userId not found for cmd={}, sessionId={}", cmd, accessor.getSessionId());
      }
    }

    return message;
  }

  private String getUserId(StompHeaderAccessor accessor) {
    if (accessor.getSessionAttributes() != null) {
      Object id = accessor.getSessionAttributes().get("userId");
      if (id instanceof String s && !s.isBlank()) return s;
    }
    if (accessor.getUser() != null) {
      String name = accessor.getUser().getName();
      if (name != null && !name.isBlank()) return name;
    }
    return null;
  }
}