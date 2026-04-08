package com._labor.fakecord.domain.events;

import java.util.UUID;

public record UserStatusChangedEvent(
  UUID userId, 
  int newMask, 
  boolean isInitialConnect
) {}
