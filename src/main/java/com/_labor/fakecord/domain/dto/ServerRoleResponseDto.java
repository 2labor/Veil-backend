package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record ServerRoleResponseDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  String name,
  boolean hoist,
  String colorHex,
  @JsonSerialize(using = ToStringSerializer.class)
  Long permissions
) {}