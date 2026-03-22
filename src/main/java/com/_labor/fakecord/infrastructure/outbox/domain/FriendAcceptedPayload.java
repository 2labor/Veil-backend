package com._labor.fakecord.infrastructure.outbox.domain;

import java.util.UUID;

public record FriendAcceptedPayload(
  UUID requesterId, 
  UUID targetId
) {}
