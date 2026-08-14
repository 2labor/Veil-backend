package com._labor.fakecord.services.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.PermissionMetadataDto;
import com._labor.fakecord.domain.dto.UserServerPermissionsDto;
import com._labor.fakecord.domain.entity.ChannelPermissionOverride;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.domain.policy.ChannelPermissionCalculator;
import com._labor.fakecord.repository.ChannelPermissionOverrideRepository;
import com._labor.fakecord.repository.ServerMemberRepository;
import com._labor.fakecord.services.PermissionService;
import com._labor.fakecord.services.ServerSecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
  
  private final ServerMemberRepository repository;
  private final ServerSecurityService serverSecurityService;
  private final ChannelPermissionOverrideRepository overrideRepository;
  private final ChannelPermissionCalculator permissionCalculator;

  @Transactional(readOnly = true)
  @Override
  public long getEffectivePermissions(UUID userId, Long serverId) {
    if (serverSecurityService.isUserOwner(userId, serverId)) { 
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

  @Override
  public List<PermissionMetadataDto> getAllPermissionsMetadata() {
    return Arrays.stream(ServerRolePermissions.values())
      .map(PermissionMetadataDto::fromEnum)
      .toList();
  }

  @Override
  public UserServerPermissionsDto getUserPermissionsOnServer(UUID userId, Long serverId) {
    boolean isOwner = serverSecurityService.isUserOwner(userId, serverId);
    long rawMask = getEffectivePermissions(userId, serverId);

    return new UserServerPermissionsDto(serverId, rawMask, isOwner);
  }

  @Override
  public long getEffectiveChannelPermissions(UUID userId, Long serverId, Long channelId) {
    long basePermission = getEffectivePermissions(userId, serverId);

    if (ServerRolePermissions.isGranted(basePermission, ServerRolePermissions.ADMIN_ACCESS)) {
      return ~0L;
    }

    ServerMemberId memberId = new ServerMemberId(userId, serverId);
    ServerMember member = repository.findByIdWithRoles(memberId)
      .orElseThrow(() -> new IllegalArgumentException("No member with such id"));
    Set<String> userRoleIds = member.getRoles().stream()
      .map(role -> role.getId().toString())
      .collect(Collectors.toSet());

    List<ChannelPermissionOverride> overrides = overrideRepository.findByChannelId(channelId);

    return permissionCalculator.calculateChannelPermission(userId, serverId, basePermission, userRoleIds, overrides);
  }

  @Override
  public boolean hasChannelPermission(UUID userId, Long serverId, Long channelId, ServerRolePermissions rolePermission) {
    long effectiveMask = getEffectiveChannelPermissions(userId, serverId, channelId);
    return ServerRolePermissions.isGranted(effectiveMask, rolePermission);
  }

  @Override
  public void requestChannelPermission(UUID userId, Long serverId, Long channelId, ServerRolePermissions rolePermission) {
    if (!hasChannelPermission(userId, serverId, channelId, rolePermission)) {
      throw new AccessDeniedException("You don't have permission '" + rolePermission.getTitle() + "' in this channel");
    }
  }

  @Override
  public Set<Long> getAccessibleChannels(UUID operatorId, Long serverId, List<Long> channelIds, ServerRolePermissions permissions) {
    if (channelIds == null || channelIds.isEmpty()) return Set.of();

    long basePermission = getEffectivePermissions(operatorId, serverId);
    if (ServerRolePermissions.isGranted(basePermission, ServerRolePermissions.ADMIN_ACCESS)) {
      return new HashSet<>(channelIds);
    }

    ServerMemberId memberId = new ServerMemberId(operatorId, serverId);
    ServerMember member = repository.findByIdWithRoles(memberId)
      .orElseThrow(() -> new AccessDeniedException("User is not a member of this server"));
    
    Set<String> userRoleIds = member.getRoles().stream()
      .map(role -> role.getId().toString())
      .collect(Collectors.toSet());

    List<ChannelPermissionOverride> allOverrides = overrideRepository.findByChannelIdIn(channelIds);

    Map<Long, List<ChannelPermissionOverride>> overridesByChannel = allOverrides.stream()
      .collect(Collectors.groupingBy(ChannelPermissionOverride::getChannelId));

    Set<Long> allowedChannelIds = new HashSet<>();
    for (Long channelId : channelIds) {
      List<ChannelPermissionOverride> channelOverrides = overridesByChannel.getOrDefault(channelId, List.of());

      long effectiveMask = permissionCalculator.calculateChannelPermission(operatorId, serverId, basePermission, userRoleIds, channelOverrides);
      if (ServerRolePermissions.isGranted(effectiveMask, permissions)) {
        allowedChannelIds.add(channelId);
      }
    }

    return allowedChannelIds;
  }

}