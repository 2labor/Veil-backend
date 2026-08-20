package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.InviteResponseDto;
import com._labor.fakecord.domain.dto.ServerInviteCreateDto;
import com._labor.fakecord.domain.dto.ServerInviteResponseDto;
import com._labor.fakecord.domain.entity.ServerInvite;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.domain.mappper.ServerInviteMapper;
import com._labor.fakecord.security.permissions.RequirePermission;
import com._labor.fakecord.services.ServerInviteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class ServerInviteController {
 
  private final ServerInviteService service;
  private final ServerInviteMapper mapper;
  
  @PostMapping("/servers/{serverId}/invites")
  @RequirePermission(ServerRolePermissions.CREATE_INSTANT_INVITE)
  public ResponseEntity<ServerInviteResponseDto> createInvite(
    Principal principal,
    @PathVariable Long serverId,
    @RequestBody(required = false) ServerInviteCreateDto dto
  ) {
    UUID userId = getUserId(principal);
    ServerInvite inviteEntity = mapper.toEntity(dto);
    ServerInvite savedInvite = service.createInvite(userId, serverId, inviteEntity);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapper.toDto(savedInvite));
  }

  @PostMapping("/invites/{code}/join")
  public ResponseEntity<Void> acceptInvite(
    Principal principal,
    @PathVariable String code
  ) {
    UUID userId = getUserId(principal);
    service.acceptInvite(userId, code);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/servers/{serverId}/invites/{code}")
  @RequirePermission(ServerRolePermissions.MANAGE_INVITES)
  public ResponseEntity<Void> removeInvite(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable String code
  ) {
    UUID userId = getUserId(principal);
    service.removeInvite(userId, serverId, code);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/invites/{code}")
  public ResponseEntity<InviteResponseDto> getServerInvitePreview(
    @PathVariable String code
  ) {
    return ResponseEntity.ok(service.getInvitePreview(code));
  }

  @GetMapping("/servers/{serverId}/invites")
  @RequirePermission(ServerRolePermissions.MANAGE_INVITES)
  public ResponseEntity<List<ServerInviteResponseDto>> getAllServerInvites(
    Principal principal,
    @PathVariable Long serverId
  ) {
    UUID userId = getUserId(principal);
    List<ServerInvite> invites = service.getAllServerInvites(userId, serverId);
    return ResponseEntity.ok(mapper.toListDto(invites));

  }

  private UUID getUserId(Principal principal) {
    if (principal == null) return null;
    return UUID.fromString(principal.getName());
  }
}