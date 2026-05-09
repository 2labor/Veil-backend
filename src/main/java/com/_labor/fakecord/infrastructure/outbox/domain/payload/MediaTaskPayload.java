package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.dto.AttachmentTask;

public record MediaTaskPayload(
  UUID ownerId,
  List<AttachmentTask> items
) {}
