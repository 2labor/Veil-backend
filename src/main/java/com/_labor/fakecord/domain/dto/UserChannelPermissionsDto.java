package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record UserChannelPermissionsDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long channelId,
  @JsonSerialize(using = ToStringSerializer.class)
  Long serverId,
  @JsonSerialize(using = ToStringSerializer.class)
  long permissionsMask
) {}