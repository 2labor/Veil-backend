package com._labor.fakecord.infrastructure.outbox.domain;

import java.util.UUID;

public record UserBlockPayload(
  UUID actorId, 
  UUID targetId,
  String reason
) {}
