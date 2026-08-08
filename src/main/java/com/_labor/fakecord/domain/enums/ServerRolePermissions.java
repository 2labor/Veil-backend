package com._labor.fakecord.domain.enums;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import com._labor.fakecord.domain.entity.ServerRole;

import lombok.Getter;

@Getter
public enum ServerRolePermissions {
  READ_CHANNEL(1L << 0, "Read chat content"),
  WRITE_TO_CHANNEL(1L << 1, "Write to channel"),
  ADD_ATTACHMENTS(1L << 2, "Add attachments to the message"),
  MANAGE_MESSAGES(1L << 3, "Menage users messages"),
  MANAGE_CHANNELS(1L << 4, "Menage server channels"),
  MANAGE_ROLES(1L << 5, "Menage server roles"),
  MANAGE_EMOJIS(1L << 6, "Menage server emojis"),
  MANAGE_USERS(1L << 7, "Manage users on a server"),
  ADMIN_ACCESS(1L << 8, "Admin access on the server"),
  CREATE_INSTANT_INVITE(1L << 9, "Create new invites to the server"),
  MANAGE_INVITES(1L << 10, "Manage server invites");
  
  private final Long mask;
  private final String title;

  ServerRolePermissions(Long mask, String title) {
    this.mask = mask;
    this.title = title;
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
