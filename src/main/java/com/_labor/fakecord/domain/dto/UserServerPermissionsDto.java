package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record UserServerPermissionsDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long serverId,
  @JsonSerialize(using = ToStringSerializer.class)
  Long rawMask,
  boolean isOwner
){}
