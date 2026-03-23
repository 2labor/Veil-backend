package com._labor.fakecord.domain.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record DirectMessageChannelDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  UserProfileShort recipient,
  String lastMessageContent,
  Instant lastActivity,
  int unreadCount
) {}