package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record DirectMessageChannelDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  UserProfileShort recipient,
  String lastMessageContent,
  Long lastActivity,
  int unreadCount
) {}