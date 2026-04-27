package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com._labor.fakecord.services.TypingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TypingSocketController {
  
  private final TypingService typingService;

  @MessageMapping("/channels.{channelId}.typing")
  public void handleMessage(
    @DestinationVariable Long channelId, 
    Principal principal
  ) {
    if (principal == null) return;

    try {
      UUID userId = UUID.fromString(principal.getName());
      
      typingService.handleTyping(channelId, userId);
    } catch (Exception e) {
      log.error("Error handling typing event for channel {}: {}", channelId, e.getMessage());
    }
  }  

  @MessageExceptionHandler
  public void handleException(Exception e) {
      log.error("WebSocket typing error: ", e);
  }
  
}
