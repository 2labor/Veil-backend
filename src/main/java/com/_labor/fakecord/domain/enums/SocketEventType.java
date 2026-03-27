package com._labor.fakecord.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocketEventType {
  MESSAGE_CREATE("MESSAGE_CREATE"),
  MESSAGE_UPDATE("MESSAGE_UPDATE"),
  MESSAGE_DELETE("MESSAGE_DELETE"),
  DM_CREATE("DM_CREATE"),
  GROUP_CREATE("GROUP_CREATE"),
  MEMBER_JOIN("MEMBER_JOIN"),
  MEMBER_LEAVE("MEMBER_LEAVE"), 
  TYPING_START("TYPING_START");

  private final String value;

  @JsonValue
  public String getValue() {
    return value;
  }

}
