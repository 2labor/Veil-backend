package com._labor.fakecord.services;

import java.util.UUID;

import com._labor.fakecord.domain.dto.UserProfileFullDto;

public interface UserProfileCache {
  UserProfileFullDto getUserProfile(UUID userId);
  void evict(UUID userId);
}