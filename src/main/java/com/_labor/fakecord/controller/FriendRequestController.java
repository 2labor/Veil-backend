package com._labor.fakecord.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.FriendRequestSummary;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.enums.RequestSource;
import com._labor.fakecord.services.FriendRequestCommandService;
import com._labor.fakecord.services.FriendRequestQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1/friend-request")
@RequiredArgsConstructor
@Slf4j
public class FriendRequestController {
  
  private final FriendRequestCommandService commandService;
  private final FriendRequestQueryService queryService;

  @PostMapping("/send/{targetId}")
  public ResponseEntity<Void> sendRequest(
    @AuthenticationPrincipal UserDetails currentUserId,
    @PathVariable UUID targetId,
    @RequestParam(defaultValue = "SEARCH") RequestSource source
  ) {
    UUID userId = getUserId(currentUserId);
    commandService.sendFriendRequest(userId, targetId, source);
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/accept/{targetId}")
  public ResponseEntity<Void> acceptRequest(
    @AuthenticationPrincipal UserDetails currentUserId,
    @PathVariable UUID targetId
  ) {
    UUID userId = getUserId(currentUserId);

    commandService.acceptFriendRequest(userId, targetId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/cancel/{targetId}")
  public ResponseEntity<Void> cencelOrDecline(
    @AuthenticationPrincipal UserDetails currentUserId,
    @PathVariable UUID targetId
  ) {
    UUID userId = getUserId(currentUserId);

    commandService.declineOrCancelRequest(userId, targetId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/ignore/{requesterId}")
  public ResponseEntity<Void> ignoreRequest(
    @AuthenticationPrincipal UserDetails currentUserId,
    @PathVariable UUID requesterId
  ) {
    UUID userId = getUserId(currentUserId);

    commandService.ignoreRequest(userId, requesterId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/incoming")
  public ResponseEntity<Slice<UserProfileShort>> getIncoming(
    @AuthenticationPrincipal UserDetails currentUserId,
    Pageable pageable
  ) {
    UUID userId = getUserId(currentUserId);

    return ResponseEntity.ok(queryService.getIncomingRequests(userId, pageable));
  }

  @GetMapping("/outgoing")
  public ResponseEntity<Slice<UserProfileShort>> getOutgoing(
    @AuthenticationPrincipal UserDetails currentUserId,
    Pageable pageable
  ) {

    UUID userId = getUserId(currentUserId);

    return ResponseEntity.ok(queryService.getOutgoingRequests(userId, pageable));
  }

  @GetMapping("/summary")
  public ResponseEntity<FriendRequestSummary> getRequestCounter(
    @AuthenticationPrincipal UserDetails userDetails
  ) {
    UUID userId = getUserId(userDetails);
    long incoming = queryService.getCounterIncomingRequests(userId);
    long outgoing = queryService.getCounterOutgoingRequests(userId);

    return ResponseEntity.ok(new FriendRequestSummary(
      incoming,
      outgoing
    ));
  }

  private UUID getUserId(UserDetails userDetails) {
    return UUID.fromString(userDetails.getUsername());
  }
}
