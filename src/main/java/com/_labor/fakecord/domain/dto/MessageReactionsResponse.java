package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record MessageReactionsResponse(
  @JsonSerialize(using = ToStringSerializer.class)
  Long emojiId,
  @JsonSerialize(using = ToStringSerializer.class)
  Long counter,
  boolean selfReacted
) {}