package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record GroupChannelDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  String name,
  UUID ownerId, 
  String lastMessageContent,
  Long lastActivity,
  int unreadCount
) {}