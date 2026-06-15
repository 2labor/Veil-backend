package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record EmojiResponse(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,  
  String name,
  String category,
  @JsonSerialize(using = ToStringSerializer.class)
  Long serverId,
  String url,
  boolean isAnimated,
  Long createAt
) {}