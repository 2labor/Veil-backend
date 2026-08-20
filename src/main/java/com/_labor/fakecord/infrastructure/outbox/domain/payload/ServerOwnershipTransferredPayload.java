package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;

public record ServerOwnershipTransferredPayload(
  Long serverId,
  UUID oldOwnerId,
  UUID newOwnerId
) {}