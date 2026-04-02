package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;

public record MessageCreatedPayload(
  Long messageId, 
  Long channelId, 
  UUID authorId,
  String authorName,
  String content
) {}
