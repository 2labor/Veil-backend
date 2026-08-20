package com._labor.fakecord.domain.dto;

import lombok.Builder;

@Builder
public record InviteResponseDto(
  String serverName,
  String description,
  String iconUrl,
  String bannerUrl,
  int memberCounter
) {}