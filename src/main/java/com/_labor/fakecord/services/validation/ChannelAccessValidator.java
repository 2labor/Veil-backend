package com._labor.fakecord.services.validation;

import java.util.UUID;

public interface ChannelAccessValidator {
  void accessValidation(Long channelId, UUID userId);
}
