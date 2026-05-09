package com._labor.fakecord.domain.dto;

import java.util.UUID;

public record AttachmentUploadResponse(
  UUID attachmentId,
  String uploadUrl
) {}
