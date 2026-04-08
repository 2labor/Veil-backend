package com._labor.fakecord.domain.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
  ONLINE(0, "online"),
  IDLE(1, "idle"),
  DO_NOT_DISTURB(2, "dnd"),
  INVISIBLE(3, "invisible"),
  OFFLINE(4, "offline");

  private final int code;
  private final String value;

  UserStatus(int code, String value) {
    this.code = code;
    this.value = value;
  }

  public static UserStatus fromCode(int code) {
    for (UserStatus status : values()) {
      if (status.getCode() == code) {
        return status;
      }
    }
    return OFFLINE;
  }
}
