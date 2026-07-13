package com._labor.fakecord.domain.enums;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

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
  ADMIN_ACCESS(1L << 7, "Admin access on the server");

  private final Long mask;
  private final String title;

  ServerRolePermissions(Long mask, String title) {
    this.mask = mask;
    this.title = title;
  }

  public static boolean isGranted(Long rawMask, ServerRolePermissions permission) {
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
      if (isGranted(rawMask, p)) {
        permissions.add(p);
      }
    }
    return permissions;
  }
}
