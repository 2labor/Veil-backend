package com._labor.fakecord.domain.dto;

public record ReplyPreviewDto(
  String authorName,
  String authorAvatarUrl,
  String content
) {}