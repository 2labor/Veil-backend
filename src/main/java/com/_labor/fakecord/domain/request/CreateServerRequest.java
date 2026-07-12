package com._labor.fakecord.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateServerRequest(
  @NotBlank(message = "Server name cannot be empty")
  @Size(min = 2, max = 100, message = "Server name must be between 2 and 100 characters")
  String name,
  String iconUrl
) {}