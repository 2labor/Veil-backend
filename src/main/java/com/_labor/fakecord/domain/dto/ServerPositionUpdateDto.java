package com._labor.fakecord.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record ServerPositionUpdateDto(
  @NotBlank(message = "serverId cannot be blank!")
  Long serverId,
  @NotBlank(message = "position cannot be blank!")
  Integer position
) {}