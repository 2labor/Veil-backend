package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com._labor.fakecord.domain.enums.ChannelType;

public interface MessageContext {
  ChannelType getChannelType();
  UUID getRecipientId();

  Long getServiceId();
  String getChannelName();
}
