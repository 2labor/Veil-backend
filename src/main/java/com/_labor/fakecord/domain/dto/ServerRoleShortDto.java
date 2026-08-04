package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record ServerRoleShortDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  String name,
  String colorHex,
  int position
) {}