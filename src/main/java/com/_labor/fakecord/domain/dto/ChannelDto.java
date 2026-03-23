package com._labor.fakecord.domain.dto;

import java.time.Instant;

import com._labor.fakecord.domain.enums.ChannelType;

public record ChannelDto(
  Long id,
  String name,
  ChannelType type,
  Long serverId,
  Long lastMessageId,
  Instant lastActivity
) {}