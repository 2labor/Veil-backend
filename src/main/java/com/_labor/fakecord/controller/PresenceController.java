package com._labor.fakecord.controller;

import java.util.UUID;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import com._labor.fakecord.infrastructure.presence.PresenceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PresenceController {
  
  private final PresenceService presenceService;

  @MessageMapping("/presence/viewing")
  public void startViewing(
    @Payload UUID targetUserId, 
    SimpMessageHeaderAccessor headerAccessor
  ){
    UUID observerId = getUserId(headerAccessor);
    if (observerId.equals(targetUserId)) return;
    presenceService.addViewer(targetUserId, observerId);
    log.trace("User {} started viewing profile of {}", observerId, targetUserId);
  }

  @MessageMapping("/presence/stop-viewing")
  public void stopViewing(
    @Payload UUID targetUserId, 
    SimpMessageHeaderAccessor headerAccessor
  ) {
    UUID observerId = getUserId(headerAccessor);
    if (observerId.equals(targetUserId)) return;
    presenceService.removeViewer(targetUserId, observerId);
  }

  private UUID getUserId(SimpMessageHeaderAccessor headerAccessor) {
    return UUID.fromString(headerAccessor.getSessionAttributes().get("userId").toString());
  }
}
