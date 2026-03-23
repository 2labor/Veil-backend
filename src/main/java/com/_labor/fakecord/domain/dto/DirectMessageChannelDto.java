package com._labor.fakecord.domain.dto;

import java.time.Instant;

public record DirectMessageChannelDto(
  Long id,
  UserProfileShort recipient,
  String lastMessageContent,
  Instant lastActivity,
  int unreadCount
) {}