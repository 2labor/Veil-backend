package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.entity.ServerRole;

public interface ServerRoleService {
  ServerRole createDefaultRole(Long serverId);
  ServerRole addRoleOnServer(UUID operatorId, Long serverId, ServerRole newRole);
  ServerRole updateRoleOnServer(UUID operatorId, Long serverId, ServerRole updatedRole);
  List<ServerRole> getAllServerRoles(UUID operatorId, Long serverId);
  ServerRole getRoleById(UUID operatorId, Long serverId, Long roleId);
  void deleteRole(UUID operatorId, Long serverId, Long targetRoleId);
  void addRolesToMember(UUID operatorId, Long serverId, UUID targetUserId, Long targetRoleId);
  void removeRoleFromMember(UUID operantId, Long serverId, UUID targetUserId, Long targetRoleId);
}