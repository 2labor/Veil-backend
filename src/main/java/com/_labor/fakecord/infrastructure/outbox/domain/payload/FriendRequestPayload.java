package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;

public record FriendRequestPayload(
    UUID senderId, 
    UUID targetId,
    String source 
) {}