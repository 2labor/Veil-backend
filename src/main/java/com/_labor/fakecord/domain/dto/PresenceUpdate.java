package com._labor.fakecord.domain.dto;

import java.util.UUID;

public record PresenceUpdate(
  UUID userId,
  int mask
) {}
