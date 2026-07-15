package com._labor.fakecord.domain.dto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com._labor.fakecord.domain.enums.ChannelType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record ChannelResponseDto(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  @JsonSerialize(using = ToStringSerializer.class)
  Long serverId, 
  ChannelType type,
  String name,
  Integer position, 
  @JsonSerialize(using = ToStringSerializer.class)
  Long parentId
) {}