package com._labor.fakecord.infrastructure.cache.services;

import java.util.UUID;
import java.util.function.Supplier;

public interface PermissionCache {
  long getServerPermission(UUID userId, Long serverId, Supplier<Long> dbFallback);
  long getChannelPermission(UUID userId, Long serverId, Long channelId, Supplier<Long> dbFallback);
  
  void evictUserServerPermission(Long serverId, UUID userId);
  void evictServerPermissionAll(Long serverId);
  void evictChannelPermissionsAll(Long serverId);
}