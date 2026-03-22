package com._labor.fakecord.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.services.UserBlockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/v1/block")
@RequiredArgsConstructor
@Slf4j
public class UserBlockController {
  
  private final UserBlockService service;

  @PostMapping("/{targetId}")
  public ResponseEntity<Void> blockUser(
    @AuthenticationPrincipal UserDetails userDetails,
    @PathVariable UUID targetId
  ){
    service.blockUser(getUserId(userDetails), targetId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{targetId}")
  public ResponseEntity<Void> unblockUser(
  @AuthenticationPrincipal UserDetails userDetails,
  @PathVariable UUID targetId) {
    service.unblockUser(getUserId(userDetails), targetId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<Slice<UserProfileShort>> getBlockUsers(
    @AuthenticationPrincipal UserDetails userDetails,
    Pageable pageable
  ) {
    return ResponseEntity.ok(service.getBlockedUsersList(getUserId(userDetails), pageable));
  }
  

  private UUID getUserId(UserDetails userDetails) {
    return UUID.fromString(userDetails.getUsername());
  }
}
