package com._labor.fakecord.domain.events;

import java.util.UUID;


public record UserBlockedEvent(
  UUID actorId, 
  UUID targetId
) {}
