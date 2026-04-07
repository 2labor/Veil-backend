package com._labor.fakecord.infrastructure.presence;

import com._labor.fakecord.domain.enums.UserStatus;

public class PresenceMask {
  private static final int STATUS_BITS = 0x0F;
  private static final int INVISIBLE_BIT = 1 << 4;

  public static int createMask(UserStatus status, boolean invisible) {
    int mask = status.getCode() & STATUS_BITS;
    if (invisible) {
      mask |= INVISIBLE_BIT;
    }
    return mask;
  }

  public static UserStatus getStatus(int mask) {
    int code = mask & STATUS_BITS;
    return UserStatus.fromCode(code);
  }

  public static boolean isInvisible(int mask) {
    return (mask & INVISIBLE_BIT) != 0;
  }

  public static UserStatus getEffectiveStatus(int mask) {
    if (mask == -1 || isInvisible(mask)) {
      return UserStatus.OFFLINE;
    }
    return getStatus(mask);
  }
}
