package com._labor.fakecord.domain.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageRequest(
  @NotBlank(message = "Content cannot be empty")
  @Size(max = 2000, message = "Message is too long")
  String content,
  @NotBlank(message = "Nonce is required for idempotency")
  String nonce,
  Long parentId,
  List<UUID> attachmentIds
) {}
