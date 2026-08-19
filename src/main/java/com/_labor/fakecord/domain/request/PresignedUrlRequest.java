package com._labor.fakecord.domain.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PresignedUrlRequest(
  @NotBlank
  String fileName,
  @NotBlank
  @Pattern(
    regexp = "^image/(png|jpeg|jpg|webp|gif)$", 
    message = "Only PNG, JPEG, WEBP, and GIF images are allowed"
  )
  String contentType,
  @NotNull
  @Max(value = 5 * 1024 * 1024, message = "File size cannot exceed 5MB")
  Long fileSize
) {}