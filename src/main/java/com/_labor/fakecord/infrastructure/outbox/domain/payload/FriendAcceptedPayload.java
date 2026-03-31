package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;

public record FriendAcceptedPayload(
  UUID requesterId, 
  UUID targetId
) {}
