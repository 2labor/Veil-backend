package com._labor.fakecord.domain.dto;

import java.util.UUID;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageEditRequest(
  @NotBlank(message = "Content cannot be empty")
  @Size(max = 2000, message = "Message is too long")
  String content,
  List<UUID> attachmentIds 
) {}
