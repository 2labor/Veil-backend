package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com._labor.fakecord.domain.enums.ChannelType;

public interface ChannelAccessInfo {
  ChannelType getChannelType();
  UUID getRecipientId();
}
