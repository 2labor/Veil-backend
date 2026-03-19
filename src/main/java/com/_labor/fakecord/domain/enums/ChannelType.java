package com._labor.fakecord.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChannelType {
  DM(1),
  GROUP_DM(2),
  GUILD_TEXT(3),
  GUILD_VOICE(4);

  private final int value;
}
