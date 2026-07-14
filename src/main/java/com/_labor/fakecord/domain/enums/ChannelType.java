package com._labor.fakecord.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelType {
  DM(1),
  GROUP_DM(2),
  GUILD_TEXT(3),
  GUILD_VOICE(4),
  GUILD_CATEGORY(5);

  private final int value;

  public boolean isGuildType() {
    return this == GUILD_TEXT | this == GUILD_VOICE | this == GUILD_CATEGORY;
  }
}
