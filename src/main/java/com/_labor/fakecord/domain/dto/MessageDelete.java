package com._labor.fakecord.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record MessageDelete(
  @JsonSerialize(using = ToStringSerializer.class)
  Long messageId  
) {}