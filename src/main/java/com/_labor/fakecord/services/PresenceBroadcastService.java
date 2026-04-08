package com._labor.fakecord.services;

import java.util.UUID;

public interface PresenceBroadcastService {
  void broadcastStatusChange(UUID userId, int newMask);
}
