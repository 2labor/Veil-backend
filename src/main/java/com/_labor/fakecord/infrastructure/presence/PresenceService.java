package com._labor.fakecord.infrastructure.presence;

import java.util.Optional;
import java.util.UUID;

public interface PresenceService {
  void enterChannel(UUID userId, Long channelId);
  void leaveChannel(UUID userId);
  public Optional<Long> getCurrentChannel(UUID userId);
  boolean isUserInChannel(UUID userId, Long channelId);  
}
