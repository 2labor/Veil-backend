package com._labor.fakecord.domain.dto;

public record PresignedUrlResponseDto(
  String uploadUrl,
  String objectPath,
  String publicUrl
) {}