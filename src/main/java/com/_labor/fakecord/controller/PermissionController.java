package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.PermissionMetadataDto;
import com._labor.fakecord.domain.dto.UserServerPermissionsDto;
import com._labor.fakecord.services.PermissionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PermissionController {

  private final PermissionService service;

  @GetMapping("/permissions/metadata")
  public ResponseEntity<List<PermissionMetadataDto>> getPermissionsMetadata() {
    return ResponseEntity.ok(service.getAllPermissionsMetadata());
  }

  @GetMapping("/servers/{serverId}/my-permissions")
  public ResponseEntity<UserServerPermissionsDto> getMyServerPermissions(
    Principal principal,
    @PathVariable Long serverId
  ) {
    UUID userId = getUserId(principal);
    return ResponseEntity.ok(service.getUserPermissionsOnServer(userId, serverId));
  }

  private UUID getUserId(Principal principal) {
    if (principal == null) {
      throw new IllegalStateException("User is not authenticated");
    }
    return UUID.fromString(principal.getName());
  }
}
