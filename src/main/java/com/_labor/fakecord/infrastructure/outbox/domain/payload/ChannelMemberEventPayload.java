package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;

public record ChannelMemberEventPayload(
  UUID operatorId,
  String operatorName,
  UUID targetId,
  String targetName,
  Long channelId,
  String channelName
) {}