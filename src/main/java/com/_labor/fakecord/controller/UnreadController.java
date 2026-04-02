package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.UnreadCounterService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/unread")
@RequiredArgsConstructor
public class UnreadController {
  
  private final UnreadCounterService service;
  private final ChannelMemberService channelMemberService;

  @GetMapping
  public ResponseEntity<Map<Long, Integer>> getMyCounter(
    Principal principal
  ) {
    UUID userId = getUserId(principal);
    Map<Long, Integer> counters = service.getAllCounters(userId);
    
    if (counters.isEmpty()) {
       service.syncAllFromDb(userId);
    }
    return ResponseEntity.ok(counters);
  }

  @PatchMapping("/{channelId}")
public ResponseEntity<Void> resetCounter(
    Principal principal, 
    @PathVariable Long channelId,
    @RequestParam(required = false) Long lastMessageId
  ) {
    UUID userId = getUserId(principal);
    
    if (lastMessageId != null) {
        channelMemberService.updateLastReadMessage(channelId, userId, lastMessageId);
    }
    
    service.reset(channelId, userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/sync")
  public ResponseEntity<Void> sync(
    Principal principal
  ) {
    UUID userId = getUserId(principal);
    service.syncAllFromDb(userId);
    return ResponseEntity.accepted().build();
  }

  private UUID getUserId(Principal principal) {
    return UUID.fromString(principal.getName());
  }
}
