package com._labor.fakecord.domain.enums;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com._labor.fakecord.domain.entity.ServerRole;

import lombok.Getter;

@Getter
public enum ServerRolePermissions {
  CREATE_INSTANT_INVITE(1L << 0, "Create Instant Invite", "Allows creating invite codes for users to join this server", RolePermissionCategory.GENERAL),
  MANAGE_INVITES(1L << 1, "Manage Invites", "Allows viewing and revoking active invite codes", RolePermissionCategory.GENERAL),
  MANAGE_EMOJIS(1L << 2, "Manage Emojis", "Allows uploading, editing, and deleting custom server emojis", RolePermissionCategory.GENERAL),

  MANAGE_USERS(1L << 3, "Manage Members", "Allows kicking, banning, and modifying server nicknames", RolePermissionCategory.MEMBERSHIP),
  MANAGE_ROLES(1L << 4, "Manage Roles", "Allows creating, editing, and deleting roles below their highest role", RolePermissionCategory.MEMBERSHIP),

  READ_CHANNEL(1L << 5, "View Channels", "Allows members to view channels and read message history", RolePermissionCategory.TEXT_CHANNEL),
  WRITE_TO_CHANNEL(1L << 6, "Send Messages", "Allows members to post messages in text channels", RolePermissionCategory.TEXT_CHANNEL),
  ADD_ATTACHMENTS(1L << 7, "Attach Files", "Allows uploading images, media, and files in chat", RolePermissionCategory.TEXT_CHANNEL),
  MANAGE_MESSAGES(1L << 8, "Manage Messages", "Allows deleting or pinning messages sent by other members", RolePermissionCategory.TEXT_CHANNEL),
  MANAGE_CHANNELS(1L << 9, "Manage Channels", "Allows creating, editing, or deleting text and voice channels", RolePermissionCategory.TEXT_CHANNEL),
  MANAGE_SERVER(1L << 10, "Menage channels", "Allows update and menage server credentials", RolePermissionCategory.SERVER),
  ADMIN_ACCESS(1L << 11, "Administrator", "Grants full permissions and bypasses channel overrides. Dangerous permission!", RolePermissionCategory.ADMINISTRATION);
  
  private final Long mask;
  private final String title;
  private final String descriptions;
  private final RolePermissionCategory category;

  ServerRolePermissions(Long mask, String title, String descriptions, RolePermissionCategory category) {
    this.mask = mask;
    this.title = title;
    this.descriptions = descriptions;
    this.category = category;
  }

  public static boolean isGranted(Long rawMask, ServerRolePermissions permission) {
    if (rawMask == null || permission == null) return false;
    
    if ((rawMask & ADMIN_ACCESS.getMask()) == ADMIN_ACCESS.mask) {
      return true;
    } 
    
    return (rawMask & permission.mask) == permission.mask;
  } 

  public static Long pack(Collection<ServerRolePermissions> permissions) {
    Long raw = 0L;
    if (permissions != null) {
      for (ServerRolePermissions p : permissions) {
        raw |= p.mask;
      }
    }
    return raw;
  }

  public static Set<ServerRolePermissions> unpack(Long rawMask) {
    Set<ServerRolePermissions> permissions = EnumSet.noneOf(ServerRolePermissions.class);
    for (ServerRolePermissions p : ServerRolePermissions.values()) {
      if ((rawMask & p.getMask()) == p.getMask()) {
        permissions.add(p);
      }
    }
    return permissions;
  }

  public static long calculateOverAllPermission(Collection<ServerRole> roles) {
    if (roles == null || roles.isEmpty()) return 0L;

    long accessPermission = 0L;
    for (ServerRole role : roles) {
      accessPermission |= role.getPermissions();
    }

    if (isGranted(accessPermission, ADMIN_ACCESS)) {
      return ~0L;
    }

    return accessPermission;
  }
}
