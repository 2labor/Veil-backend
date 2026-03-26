package com._labor.fakecord.domain.dto;

import java.time.Instant;

import com._labor.fakecord.domain.enums.MessageType;

public record MessageDto (
  String id,
  String content,
  MessageType type,
  UserProfileShort author,
  String nonce,
  Instant updatesAt,
  Instant timestamp
) {}
