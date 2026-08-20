package com._labor.fakecord.infrastructure.outbox.domain.payload;

public record ServerDeletedPayload(
  Long serverId
) {}