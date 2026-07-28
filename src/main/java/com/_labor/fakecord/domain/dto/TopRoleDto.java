package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record TopRoleDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  Integer position,
  String name,
  String hexColor
) {}