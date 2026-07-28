package com._labor.fakecord.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;

public interface UserProfileCache {
  UserProfileFullDto getUserProfile(UUID userId);
  void evict(UUID userId);
  Map<UUID, UserProfileShort> getUserProfileShortBatch(List<UUID> userIds);
}