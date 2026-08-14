package com._labor.fakecord.domain.policy;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.entity.ChannelPermissionOverride;
import com._labor.fakecord.domain.enums.PermissionHolderType;
import com._labor.fakecord.domain.enums.ServerRolePermissions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ChannelPermissionCalculator {

  public long calculateChannelPermission(
    UUID userId,
    Long serverId,
    long baseServerPermissions,
    Set<String> userRoleIds,
    List<ChannelPermissionOverride> channelOverrides
  ) {
    if (ServerRolePermissions.isGranted(baseServerPermissions, ServerRolePermissions.ADMIN_ACCESS)) {
      return ~0L;
    }

    long permissions = baseServerPermissions;
    if (channelOverrides == null || channelOverrides.isEmpty()) {
      return permissions;
    }

    String everyoneRoleId = serverId.toString();
    String targetUserIdStr = userId.toString();
    for (ChannelPermissionOverride override : channelOverrides) {
      if (override.getHolderType() == PermissionHolderType.ROLE && override.getHolderId().equals(everyoneRoleId)) {
        permissions &= ~override.getDenyMask();
        permissions |= override.getAllowMask();
        break;
      }
    }

    long roleAllow = 0L;
    long roleDeny = 0L;
    for (ChannelPermissionOverride override : channelOverrides) {
      if (override.getHolderType() == PermissionHolderType.ROLE) {
        String roleId = override.getHolderId();
        if (!roleId.equals(everyoneRoleId) && userRoleIds.contains(roleId)) {
          roleAllow |= override.getAllowMask();
          roleDeny |= override.getDenyMask();
        }
      }
    }

    permissions &= ~roleDeny;
    permissions |= roleAllow;
    for (ChannelPermissionOverride override : channelOverrides) {
      if (override.getHolderType() == PermissionHolderType.MEMBER && override.getHolderId().equals(targetUserIdStr)) {
        permissions &= ~override.getDenyMask();
        permissions |= override.getAllowMask();
        break; 
      }
    }

    return permissions;
  }

}
