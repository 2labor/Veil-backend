package com._labor.fakecord.services;

import java.util.UUID;

public interface UserStatusService {
  void setOnline(UUID userId);
  void updateMask(UUID userId, int mask);
  void setOffline(UUID userId);
  void setOnlineWithPreference(UUID userId);
  boolean isOnline(UUID userId);
  int getMask(UUID userId);
  void touch(UUID userId);
}
