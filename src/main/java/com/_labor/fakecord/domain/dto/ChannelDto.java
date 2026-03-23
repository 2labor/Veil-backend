package com._labor.fakecord.domain.dto;

import java.time.Instant;

import com._labor.fakecord.domain.enums.ChannelType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record ChannelDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  String name,
  ChannelType type,
  Long serverId,
  Long lastMessageId,
  Instant lastActivity
) {}