package com._labor.fakecord.domain.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record UploadResponse(
  UUID id,
  String uploadUrl,
  String publicUrl
) {}