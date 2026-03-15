package com._labor.fakecord.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.enums.RelationshipStatus;
import com._labor.fakecord.services.RelationshipCommandService;
import com._labor.fakecord.services.RelationshipQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/relationship")
@RequiredArgsConstructor
@Slf4j
public class RelationshipController {
  
  private final RelationshipCommandService relationshipCommandService;
  private final RelationshipQueryService relationshipQueryService;

  @DeleteMapping("/friends/{friendId}")
  public ResponseEntity<Void> removeFriend(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable UUID friendId
  ) {
    UUID userId = getUserId(userDetails);
    relationshipCommandService.removeFriend(userId, friendId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/friends")
  public ResponseEntity<Slice<UserProfileShort>> getFriends(
    @AuthenticationPrincipal UserDetails userDetails,
    Pageable pageable
  ) {
    UUID userId = getUserId(userDetails);
    return ResponseEntity.ok(relationshipQueryService.getFriendsList(userId, pageable));
  }

  @GetMapping("/status/{targetId}")
  public ResponseEntity<RelationshipStatus> getStatus(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable UUID targetId
  ) {
    UUID userId = getUserId(userDetails);
    RelationshipStatus status = relationshipQueryService.getRelationshipStatus(userId, targetId);
    return ResponseEntity.ok(status);
  }

  @GetMapping("/mutual/{friendId}")
  public ResponseEntity<List<UserProfileShort>> getMutualFriends(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable UUID friendId
  ) {
    UUID userId = getUserId(userDetails);
    return ResponseEntity.ok(relationshipQueryService.getMutualFriends(userId, friendId));
  }

  @GetMapping("/mutual/{targetId}/count")
  public ResponseEntity<Long> getMutualCounter(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable UUID targetId
  ) {
    UUID userId = getUserId(userDetails);
    return ResponseEntity.ok(relationshipQueryService.getMutualFriendsCount(userId, targetId));
  }
  
  private UUID getUserId(UserDetails userDetails) {
    return UUID.fromString(userDetails.getUsername());
  }
}
