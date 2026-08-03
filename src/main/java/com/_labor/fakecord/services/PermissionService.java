package com._labor.fakecord.services;

import java.util.UUID;

import com._labor.fakecord.domain.enums.ServerRolePermissions;

public interface PermissionService {
  long getEffectivePermissions(UUID userId,  Long serverId);
  void requirePermission(UUID userId, Long serverId, ServerRolePermissions permission);
  boolean hasPermission(UUID userId, Long serverId, ServerRolePermissions permission);
  boolean canGrantThePermissions(UUID userId, Long serverId, long targetPermissions);
  void requireCanGrantPermissions(UUID userId, Long serverId, long targetPermissions);
}
