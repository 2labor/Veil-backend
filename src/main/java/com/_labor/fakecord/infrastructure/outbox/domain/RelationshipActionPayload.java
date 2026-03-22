package com._labor.fakecord.infrastructure.outbox.domain;

import java.util.UUID;

public record RelationshipActionPayload(
  UUID actorId,
  UUID targetId
) {}