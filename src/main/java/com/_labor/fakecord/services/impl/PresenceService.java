package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.services.UserStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PresenceService {

  private final UserStatusService statusService;
  private final KafkaTemplate<String, Object> kafkaTemplate;

  private final String TOPIC_PRESENCE = "user-presence-events";

  public void processUserOnline(UUID userId) {
    if (statusService.isOnline(userId)) {
      log.trace("User {} already ONLINE, skipping event", userId);
      return;
    }

    log.debug("Processing online status for user: {}", userId);
    statusService.setOnline(userId);
    kafkaTemplate.send(TOPIC_PRESENCE, userId.toString(), "CONNECTED");
  }

  public void processUserOffline(UUID userId) {
    log.debug("Processing offline status for user: {}", userId);
    statusService.setOffline(userId);
    kafkaTemplate.send(TOPIC_PRESENCE, userId.toString(), "DISCONNECTED");
  }
}