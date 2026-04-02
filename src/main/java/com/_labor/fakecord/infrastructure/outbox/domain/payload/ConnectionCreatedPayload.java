package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;

import com._labor.fakecord.domain.enums.ConnectionProvider;

public record ConnectionCreatedPayload(
  UUID userId,
  ConnectionProvider provider,
  String externalId,
  String externalName
) {}
