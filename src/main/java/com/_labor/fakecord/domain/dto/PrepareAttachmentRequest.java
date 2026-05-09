package com._labor.fakecord.domain.dto;

public record PrepareAttachmentRequest(
  String fileName,
  String contentType,
  long fileSize
) {}