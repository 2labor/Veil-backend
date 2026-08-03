package com._labor.fakecord.services.impl;

import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.repository.ServerMemberRepository;
import com._labor.fakecord.services.PermissionService;
import com._labor.fakecord.services.ServerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
  
  private final ServerMemberRepository repository;
  private final ServerService serverService;

  @Transactional(readOnly = true)
  @Override
  public long getEffectivePermissions(UUID userId, Long serverId) {
    if (serverService.isUserOwner(userId, serverId)) {
      return ~0L;
    }

    ServerMemberId id = new ServerMemberId(userId, serverId);

    ServerMember member = repository.findByIdWithRoles(id)
      .orElseThrow(() -> new AccessDeniedException("User is not a member of this server"));

    return ServerRolePermissions.calculateOverAllPermission(member.getRoles());
  }

  @Override
  public boolean hasPermission(UUID userId, Long serverId, ServerRolePermissions permission) {
    long effectivePermissions = getEffectivePermissions(userId, serverId);
    return ServerRolePermissions.isGranted(effectivePermissions, permission);
  }

  @Override
  public void requirePermission(UUID userId, Long serverId, ServerRolePermissions permission) {
    if (!hasPermission(userId, serverId, permission)) {
      throw new AccessDeniedException("You don't have permission: " + permission.getTitle());
    }
  }

  @Override
  public boolean canGrantThePermissions(UUID userId, Long serverId, long targetPermissions) {
    long currUserPermission = getEffectivePermissions(userId, serverId);

    long forbiddenPermissions = targetPermissions & ~currUserPermission;

    return forbiddenPermissions == 0;
  }

  @Override
  public void requireCanGrantPermissions(UUID userId, Long serverId, long targetPermissions) {
    long actorPermissions = getEffectivePermissions(userId, serverId);
    long forbiddenPermissions = targetPermissions & ~actorPermissions;
      if (forbiddenPermissions != 0L) {
        Set<ServerRolePermissions> missing = ServerRolePermissions.unpack(forbiddenPermissions);
        throw new AccessDeniedException("You cannot grant permissions that you do not possess: " + missing);
      }
    }
  }
