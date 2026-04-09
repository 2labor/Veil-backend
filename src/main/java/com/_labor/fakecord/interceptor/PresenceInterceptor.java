package com._labor.fakecord.interceptor;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.Message;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.presence.PresenceService;
import com._labor.fakecord.services.UserStatusService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PresenceInterceptor implements ChannelInterceptor {

  private final UserStatusService statusService;
  private final PresenceService presenceService;
  private final Cache<UUID, Boolean> recentTouchCache;

  public PresenceInterceptor(UserStatusService statusService, PresenceService presenceService) {
    this.statusService = statusService;
    this.presenceService = presenceService;

    this.recentTouchCache = Caffeine.newBuilder()
      .expireAfterWrite(3, TimeUnit.MINUTES)
      .maximumSize(10_000)
      .build();
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || accessor.getCommand() == null) return message;

    StompCommand cmd = accessor.getCommand();

    String userIdSrt = getUserId(accessor);
    if (userIdSrt == null) return message;
    UUID userId = UUID.fromString(userIdSrt);

    if (userId != null) {
      switch (cmd) {
        case CONNECT -> {
          statusService.setOnlineWithPreference(userId);
          log.debug("Presence ONLINE: user={} cmd={}", userId, cmd);
        }

        case SUBSCRIBE -> {
          String destination = accessor.getDestination();
          touchStatus(userId);
          handleChannelSubscription(userId, destination);
        }

        case SEND -> {
        touchStatus(userId);
        }

        case UNSUBSCRIBE -> {
          presenceService.leaveChannel(userId);
          log.debug("User {} stopped watching channel", userId);
        }

        case DISCONNECT -> {
          statusService.setOffline(userId); 
          presenceService.leaveChannel(userId);
          recentTouchCache.invalidate(userId);
          log.debug("User {} went OFFLINE", userId);
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

  private void touchStatus(UUID userId) {
    recentTouchCache.get(userId, key -> {
      int currentMask = statusService.getMask(key);
      if (currentMask != -1) {
        statusService.touch(key);
      } else {
        statusService.setOnline(userId);
      }
      log.trace("Presence: Throttled touch for user {}", key);
      return true;
    });
  }

  private void handleChannelSubscription(UUID userId, String destination) {
    if (destination != null && destination.startsWith("/topic/channel.")) {
      try {
          String channelIdStr = destination.substring("/topic/channel.".length());
          Long channelId = Long.valueOf(channelIdStr);

          presenceService.enterChannel(userId, channelId);
          log.debug("User {} is now WATCHING channel {}", userId, channelId);
      } catch (Exception e) {
        log.warn("Presence: invalid channel format in destination: {}", destination);
      }
    }
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