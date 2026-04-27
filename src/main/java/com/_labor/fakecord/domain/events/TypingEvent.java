package com._labor.fakecord.domain.events;

import java.io.Serializable;

public record TypingEvent(
  String channelId,
  String userId,
  String userName,
  long timestamp
) implements Serializable {}