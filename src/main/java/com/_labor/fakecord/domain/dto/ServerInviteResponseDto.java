package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record ServerInviteResponseDto(
  String code,
  @JsonSerialize(using = ToStringSerializer.class)
  Long serverId,
  @JsonSerialize(using = ToStringSerializer.class)
  UUID creatorId,
  Integer maxUsed,
  Integer countUsed,
  Long expiredAt,
  Long createdAt
){}
