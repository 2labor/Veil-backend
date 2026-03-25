package com._labor.fakecord.domain.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ChannelMemberDto(
  UserProfileShort profile,
  Long lastReadMessageId,
  Instant joinAt
) {}