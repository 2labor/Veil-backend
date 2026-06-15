package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record ChannelMemberEventPayload(
  UUID operatorId,
  String operatorName,
  UUID targetId,  
  String targetName,
  @JsonSerialize(using = ToStringSerializer.class)
  Long channelId,
  String channelName
) {}