package com._labor.fakecord.domain.events;

import java.util.UUID;

import com._labor.fakecord.domain.enums.ReactionAction;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record ReactionEvent(
  @JsonSerialize(using = ToStringSerializer.class)
  Long id,
  @JsonSerialize(using = ToStringSerializer.class)
  Long messageId,
  UUID userId,
  @JsonSerialize(using = ToStringSerializer.class)
  Long emojiId,
  ReactionAction action
) {}