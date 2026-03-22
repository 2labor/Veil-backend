package com._labor.fakecord.infrastructure.outbox.domain;

import java.util.UUID;

public record FriendRequestPayload(
    UUID senderId, 
    UUID targetId,
    String source 
) {}