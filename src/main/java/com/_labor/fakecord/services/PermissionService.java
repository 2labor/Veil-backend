package com._labor.fakecord.services;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com._labor.fakecord.domain.dto.PermissionMetadataDto;
import com._labor.fakecord.domain.dto.UserChannelPermissionsDto;
import com._labor.fakecord.domain.dto.UserServerPermissionsDto;
import com._labor.fakecord.domain.enums.ServerRolePermissions;

public interface PermissionService {
  List<PermissionMetadataDto> getAllPermissionsMetadata();
  UserServerPermissionsDto getUserPermissionsOnServer(UUID userId, Long serverId);
  UserChannelPermissionsDto getUserChannelPermission(UUID userId, Long serverId, Long chanelId);

  long getEffectivePermissions(UUID userId,  Long serverId);
  void requirePermission(UUID userId, Long serverId, ServerRolePermissions permission);
  boolean hasPermission(UUID userId, Long serverId, ServerRolePermissions permission);
  boolean canGrantThePermissions(UUID userId, Long serverId, long targetPermissions);
  void requireCanGrantPermissions(UUID userId, Long serverId, long targetPermissions);

  //channels
  long getEffectiveChannelPermissions(UUID userId, Long serverId, Long channelId);
  boolean hasChannelPermission(UUID userId, Long serverId, Long channelId, ServerRolePermissions rolePermission);
  void requestChannelPermission(UUID userId, Long serverId, Long channelId, ServerRolePermissions rolePermission);


  Set<Long> getAccessibleChannels(UUID operatorId, Long serverId, List<Long> channelIds, ServerRolePermissions permissions);
}
