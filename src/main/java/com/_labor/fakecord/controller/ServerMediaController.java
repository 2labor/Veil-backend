package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.PresignedUrlResponseDto;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.domain.request.PresignedUrlRequest;
import com._labor.fakecord.security.permissions.RequirePermission;
import com._labor.fakecord.services.ServerMediaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/servers/{serverId}")
@Validated
public class ServerMediaController {
  
  private final ServerMediaService service;

  @PostMapping("/icon/presign")
  @RequirePermission(ServerRolePermissions.MANAGE_SERVER)
  public ResponseEntity<PresignedUrlResponseDto> getIconUploadUrl(
    Principal principal,
    @PathVariable Long serverId,
    @Valid @RequestBody PresignedUrlRequest request
  ) {
    UUID userId = getUserId(principal);

    PresignedUrlResponseDto response = service.generateServerIconUploadUrl(userId, serverId, request.fileName(), request.contentType(), request.fileSize());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/banner/presign")
  @RequirePermission(ServerRolePermissions.MANAGE_SERVER)
  public ResponseEntity<PresignedUrlResponseDto> getBannerUrl(
    Principal principal,
    @PathVariable Long serverId,
    @Valid @RequestBody PresignedUrlRequest request
  ) {
    UUID userId = getUserId(principal);
    
    PresignedUrlResponseDto response = service.generateServerBannerUploadUrl(userId, serverId, request.fileName(), request.contentType(), request.fileSize());

    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/icon")
  @RequirePermission(ServerRolePermissions.MANAGE_SERVER)
  public ResponseEntity<Void> deleteServerIcon(
    Principal principal,
    @PathVariable Long serverId
  ) {
    UUID userId = getUserId(principal);
    service.deleteServerIcon(userId, serverId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/banner")
  @RequirePermission(ServerRolePermissions.MANAGE_SERVER)
  public ResponseEntity<Void> deleteServerBanner(
    Principal principal,
    @PathVariable Long serverId
  ) {
    UUID userId = getUserId(principal);
    service.deleteServerBanner(userId, serverId);
    return ResponseEntity.noContent().build();
  }

  private UUID getUserId(Principal principal) {
    if (principal == null) {
      throw new AccessDeniedException("You have to be authorize!");
    }

    return UUID.fromString(principal.getName());
  }

}
