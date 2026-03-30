package com._labor.fakecord.domain.dto;

import com._labor.fakecord.domain.enums.MessageType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
public record MessageDto (
  @JsonSerialize(using = ToStringSerializer.class)
  String id,
  String content,
  MessageType type,
  UserProfileShort author,
  String nonce,
  Long updatesAt,
  Long timestamp
) {}
