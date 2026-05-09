package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com._labor.fakecord.domain.enums.AttachmentType;

public record AttachmentTask(
  UUID attachmentId,
  String storageName,
  AttachmentType type,
  String contentType 
) {}
