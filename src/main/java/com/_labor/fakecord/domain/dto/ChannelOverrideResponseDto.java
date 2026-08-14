package com._labor.fakecord.domain.dto;

import com._labor.fakecord.domain.enums.PermissionHolderType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record ChannelOverrideResponseDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  @JsonSerialize(using = ToStringSerializer.class)
  Long channelId, 
  String holderId,
  PermissionHolderType holderType,
  @JsonSerialize(using = ToStringSerializer.class)
  Long allowMask,
  @JsonSerialize(using = ToStringSerializer.class)
  Long denyMask
) {}