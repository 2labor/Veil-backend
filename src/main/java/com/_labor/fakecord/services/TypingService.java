package com._labor.fakecord.services;

import java.util.UUID;

public interface TypingService {
  void handleTyping(Long channelId, UUID userId);
  void stopTyping(Long channelId, UUID userId);
}
