package com._labor.fakecord.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.ServerMemberResponseDto;
import com._labor.fakecord.domain.dto.ServerMemberSidebarResponseDto;
import com._labor.fakecord.services.ServerMemberFacade;
import com._labor.fakecord.services.ServerMemberService;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/v1/servers/{serverId}/members")
@RequiredArgsConstructor
public class ServerMemberController {
  private final ServerMemberService service;
  private final ServerMemberFacade facadeService;

  @GetMapping("/sidebar")
  public ResponseEntity<Slice<ServerMemberSidebarResponseDto>> getSideBarUsers(
    Principal principal,
    @PathVariable Long serverId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam (defaultValue = "30") int limit
  ) {
    UUID userId = getUserId(principal);

    int size = Math.min(limit, 40);

    Pageable pageable = PageRequest.of(page, size, Sort.by("userLocalName").ascending());

    Slice<ServerMemberSidebarResponseDto> sidebarSlice = service.getServerMembers(serverId, userId, pageable);

    return ResponseEntity.ok(sidebarSlice);
  }

  @GetMapping("/{targetMemberId}")
  public ResponseEntity<ServerMemberResponseDto> getMemberProfile(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable UUID targetMemberId
  ) {
    UUID userId = getUserId(principal);
    ServerMemberResponseDto dto = facadeService.getMemberProfile(userId, targetMemberId, serverId);
    return ResponseEntity.ok(dto);
  }

  @DeleteMapping("/leave")
  public ResponseEntity<Void> leaveTheServer(
    Principal principal,
    @PathVariable Long serverId
  ) {
    UUID userId = getUserId(principal);
    service.removeMemberFromServer(userId, serverId);

    return ResponseEntity.noContent().build();
  }

  private UUID getUserId(Principal principal) {
    if (principal == null) return null;

    return UUID.fromString(principal.getName());
  }
}
