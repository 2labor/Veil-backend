package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com._labor.fakecord.domain.enums.AttachmentType;

import lombok.Builder;

@Builder
public record AttachmentDto(
  UUID id,
  String fileName,
  String url,
  String thumbnailUrl,
  AttachmentType type,
  Long fileSize,
  String metadata
) {}
