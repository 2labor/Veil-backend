package com._labor.fakecord.infrastructure.outbox.domain;

import java.util.UUID;

public record UserBlockPayload(
  UUID blockerId, 
  UUID blockedId,
  String reason
) {}
