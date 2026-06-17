package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.MessageReactionsResponse;
import com._labor.fakecord.domain.events.ReactionEvent;
import com._labor.fakecord.services.ReactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/reactions")
@RequiredArgsConstructor
@Slf4j
public class ReactionController {
  
  private final ReactionService service;

  @PostMapping("/{messageId}/reaction/{emojiId}")
  public ResponseEntity<ReactionEvent> toggleReactions(
    @PathVariable Long messageId, 
    @PathVariable Long emojiId, 
    Principal principal
  ) {
    log.info("REST request to toggle reaction {} for message {}", emojiId, messageId);
    UUID userId = getUserId(principal);

    ReactionEvent event = service.toggleReaction(messageId, emojiId, userId);
    return ResponseEntity.ok(event);
  }

  @DeleteMapping("/{messageId}/reactions")
  public ResponseEntity<Void> clearAllReactionOnMessage(
    @PathVariable Long messageId,
    Principal principal
  ) {
    UUID userId = getUserId(principal);
    log.info("REST request to clear all reactions for message {} by operator {}", messageId, userId);
    service.clearAllReactions(messageId, userId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{messageId}")
  public ResponseEntity<List<MessageReactionsResponse>> getReactions(
    @PathVariable Long messageId,
    Principal principal
  ) {
    UUID userId = getUserId(principal);
    List<MessageReactionsResponse> reactions = service.getAllReactionsForMessage(messageId, userId);
    return ResponseEntity.ok(reactions);
  }

  private UUID getUserId(Principal principal) {
    return UUID.fromString(principal.getName());
  }
}
