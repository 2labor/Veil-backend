package com._labor.fakecord.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.ChannelOverrideResponseDto;
import com._labor.fakecord.domain.entity.ChannelPermissionOverride;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.domain.mappper.ChannelPermissionOverrideMapper;
import com._labor.fakecord.domain.request.SetChannelOverrideRequest;
import com._labor.fakecord.security.permissions.RequirePermission;
import com._labor.fakecord.services.ChannelPermissionOverrideService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/v1/servers/{serverId}/channels/{channelId}/permissions")
@RequiredArgsConstructor
public class ChannelOverrideController {
  
  private final ChannelPermissionOverrideService service;
  private final ChannelPermissionOverrideMapper mapper;

  @GetMapping()
  @RequirePermission(value = ServerRolePermissions.MANAGE_CHANNELS, channelId = "#channelId")
  public ResponseEntity<List<ChannelOverrideResponseDto>> getChannelPermissions(
    Principal principal,
    @PathVariable Long serverId, 
    @PathVariable Long channelId
  ) {
    UUID operatorId = getUserId(principal);
    List<ChannelPermissionOverride> channelPermissions = service.getChannelPermissionsOverride(operatorId, serverId, channelId);

    return ResponseEntity.ok(mapper.toResponseDtos(channelPermissions));
  }

  @PutMapping("/{holderId}")
  @RequirePermission(value = ServerRolePermissions.MANAGE_CHANNELS, channelId = "#channelId")
  public ResponseEntity<ChannelOverrideResponseDto> setChannelPermissions(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable Long channelId,
    @PathVariable String holderId,
    @Valid @RequestBody SetChannelOverrideRequest request
  ) {
    UUID operatorId = getUserId(principal);
    ChannelPermissionOverride override = service.setChannelPermissionOverride(operatorId, serverId, channelId, holderId, request);

    return ResponseEntity.ok(mapper.toResponseDto(override));
  }

  @DeleteMapping("/{holderId}")
  @RequirePermission(value = ServerRolePermissions.MANAGE_CHANNELS, channelId = "#channelId")
  public ResponseEntity<Void> deleteChannelPermission(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable Long channelId,
    @PathVariable String holderId
  ) {
    UUID operatorId = getUserId(principal);
    service.deleteChannelPermissionOverride(operatorId, serverId, channelId, holderId);
    return ResponseEntity.noContent().build();
  }

  private UUID getUserId(Principal principal) {
    if (principal == null) throw new AccessDeniedException("User is not authenticated");

    return UUID.fromString(principal.getName());
  }
    
}
