package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.PresenceUpdate;
import com._labor.fakecord.infrastructure.presence.PresenceService;
import com._labor.fakecord.services.PresenceBroadcastService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceBroadcastServiceImpl implements PresenceBroadcastService {

  private final SimpMessagingTemplate messagingTemplate;
  private final PresenceService presenceService;

  @Override
  public void broadcastStatusChange(UUID userId, int newMask) {
    PresenceUpdate update = new PresenceUpdate(userId, newMask);

    presenceService.getCurrentChannel(userId).ifPresent(channelId -> {
      String destination = "/topic/channel." + channelId + ".presence";
      messagingTemplate.convertAndSend(destination, update);
    });

    presenceService.streamActiveViewers(userId).forEach(viewerId -> {
      messagingTemplate.convertAndSendToUser(
        viewerId.toString(), 
        "/queue/presence/updates", 
        update
      );
    });

    log.trace("Broadcasted status change for user {}: mask {}", userId, newMask);
  }
  
}
