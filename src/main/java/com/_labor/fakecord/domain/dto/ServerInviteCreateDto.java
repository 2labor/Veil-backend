package com._labor.fakecord.domain.dto;

public record ServerInviteCreateDto(
  Integer maxUsed,
  Long durationSeconds
) {}
