package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import com._labor.fakecord.domain.dto.MessageRequest;
import com._labor.fakecord.services.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MessageWSController {
  
  private final MessageService messageService;

  @MessageMapping("/channels.{channelId}.send")
  public void handleSendMessage(
    @DestinationVariable Long channelId,
    @Payload @Valid MessageRequest request,
    Principal principal
  ) {
    messageService.sendMessage(
      channelId,
      UUID.fromString(principal.getName()),
      request.content(),
      request.nonce()
    );
  }

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  public String handleException(Throwable exception) {
    return exception.getMessage();
  }
}

