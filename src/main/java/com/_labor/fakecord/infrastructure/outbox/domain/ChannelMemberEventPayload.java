package com._labor.fakecord.infrastructure.outbox.domain;

import java.util.UUID;

public record ChannelMemberEventPayload(
  UUID operatorId,
  UUID targetId,
  Long channelId,
  String channelName
) {}