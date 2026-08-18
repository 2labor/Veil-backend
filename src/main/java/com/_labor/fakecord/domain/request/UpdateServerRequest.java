package com._labor.fakecord.domain.request;

import jakarta.validation.constraints.Size;

public record UpdateServerRequest(
  @Size(min = 2, max = 100, message = "Server name must be between 2 and 100 characters")
  String name,
  @Size(max = 128, message = "Server description cannot be larger then 128 characters")
  String description,
  String bannerUrl,
  String iconUrl
) {}