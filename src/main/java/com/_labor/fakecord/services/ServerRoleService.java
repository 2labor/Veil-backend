package com._labor.fakecord.services;

import java.util.UUID;

import com._labor.fakecord.domain.entity.ServerRole;

public interface ServerRoleService {
  ServerRole createDefaultRole(Long serverId);
  ServerRole addRoleOnServer(UUID operatorId, Long serverId, ServerRole newRole);
  ServerRole updateRoleOnServer(UUID operatorId, Long serverId, ServerRole updatedRole);
  void deleteRole(UUID operatorId, Long serverId, Long targetRoleId);
  void addRolesToMember(UUID operatorId, Long serverId, UUID targetUserId, Long targetRoleId);
  void removeRoleFromMember(UUID operantId, Long serverId, UUID targetUserId, Long targetRoleId);
}