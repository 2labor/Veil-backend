package com._labor.fakecord.infrastructure.integration.spotify;

import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.enums.ConnectionProvider;
import com._labor.fakecord.infrastructure.outbox.domain.payload.ConnectionCreatedPayload;
import com._labor.fakecord.repository.UserConnectionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyPollingCoordinator {
  
  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final UserConnectionRepository repository;

  private static final String ACTIVE_POLLING_KEY = "spotify:active_polling";

  @KafkaListener(topics = "user-connection-events", groupId = "polling-group")
  public void handleConnectionEvent(String eventPayload) {
    try {
      ConnectionCreatedPayload payload = objectMapper.readValue(eventPayload, ConnectionCreatedPayload.class);

      UUID userId = payload.userId();

      if (payload.provider() == ConnectionProvider.SPOTIFY) {
        registerForPolling(userId);
      }
    } catch (Exception e) {
      log.error("Failed to parse connection event: {}", eventPayload, e);
    }
  }

  @KafkaListener(topics = "user-presence-events", groupId = "polling-presence-group")
  public void handlePresenceEvent(
    @Header(KafkaHeaders.RECEIVED_KEY) String userId, 
    @Payload String status
  ) {
    if (null == userId || null == status) return;

    log.debug("Presence change: user {} is now {}", userId, status);
    UUID userUuid = UUID.fromString(userId);

    switch (status) {

      case "CONNECTED" -> {
        boolean hasSpotify = repository.existsByUserIdAndProvider(userUuid, ConnectionProvider.SPOTIFY);
        if (hasSpotify) {
          registerForPolling(userUuid);
        }
      }

      case "DISCONNECTED" -> {
        unregisterForPolling(userUuid);
      }
      
      default -> log.trace("Status {} for user {} doesn't affect polling", status, userId);
    }
  }

  private void registerForPolling(UUID userId) {
    redisTemplate.opsForSet().add(ACTIVE_POLLING_KEY, userId.toString());
    log.info("User {} registered for active Spotify polling", userId);
  }

  private void unregisterForPolling(UUID userId) {
    redisTemplate.opsForSet().remove(ACTIVE_POLLING_KEY, userId.toString());
    log.info("User {} removed from Spotify polling (Offline)", userId);
  }
}