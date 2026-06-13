package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com._labor.fakecord.services.PinnedMessageService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com._labor.fakecord.domain.dto.MessageDto;



@RestController
@RequestMapping("/api/v1/channels/{channelId}/pins")
@RequiredArgsConstructor
@Slf4j
public class PinnedMessageController {
  
  private final PinnedMessageService service;

  @PostMapping("/{messageId}")
  public ResponseEntity<Void> pinMessage(
    @PathVariable Long channelId,
    @PathVariable Long messageId,
    Principal principal
  ) {
    UUID operatorId = getUserId(principal);
    log.info("REST request to pin message {} in channel {} by user {}", messageId, channelId, operatorId);

    service.pinMessage(channelId, messageId, operatorId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> unpinMessage(
    @PathVariable Long channelId,
    @PathVariable Long messageId,
    Principal principal
  ) {
    UUID operatorId = getUserId(principal);
    log.info("REST request to unpin message {} from channel {} by user {}", messageId, channelId, operatorId);

    service.unpinMessage(channelId, messageId, operatorId);
    return ResponseEntity.noContent().build();
  }
  
  @GetMapping
  public ResponseEntity<Slice<MessageDto>> getPinnedMessages(
    @PathVariable Long channelId,
    @RequestParam(defaultValue = "10") int limit,
    Principal principal
  ) {
    UUID currentUserId = getUserId(principal);
    Slice<MessageDto> pinnedMessages = service.getPinnedMessages(channelId, currentUserId, limit);
    return ResponseEntity.ok(pinnedMessages);
  }

  @GetMapping("/latest")
  public ResponseEntity<MessageDto> getLatestPinnedMessage(
    @PathVariable Long channelId,
    Principal principal
  ) {
    UUID currentUserId = getUserId(principal);
    MessageDto pinnedMessage = service.getLatestPinnedMessage(channelId, currentUserId);
    if (pinnedMessage == null) return ResponseEntity.noContent().build();

    return ResponseEntity.ok(pinnedMessage);
  }

  @GetMapping("/{currentMessageId}/next")
  public ResponseEntity<MessageDto> getNextPinnedMessage(
    @PathVariable Long channelId,
    @PathVariable Long currentMessageId,
    @RequestParam(defaultValue = "PREVIOUS") String direction,
    Principal principal
  ) {
    UUID currentUserId = getUserId(principal);
    MessageDto nextPin = service.getNextPinnedMessage(channelId, currentMessageId, currentUserId, direction);

    if (nextPin == null) return ResponseEntity.noContent().build();

    return ResponseEntity.ok(nextPin);
  }

  private UUID getUserId(Principal principal) {
    return UUID.fromString(principal.getName());
  }

}
