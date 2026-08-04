package com._labor.fakecord.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServerRoleUpdateDto(
  @NotBlank(message = "Name for the role cannot be empty")
  @Size(min = 3, max = 56, message = "Name have to be in range of 3 to 56 characters!")
  String name,
  boolean hoist,
  String colorHex,
  @NotNull(message = "Permissions cannot be empty!")
  Long permissions
) {}