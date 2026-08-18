package com._labor.fakecord.infrastructure.cache.services.Impl;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.cache.CacheProvider;
import com._labor.fakecord.infrastructure.cache.services.PermissionCache;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermissionCacheImpl implements PermissionCache {
  
  private final CacheProvider provider;
  private final String SERVER_PERM_PREFIX = "perm:v1:server:";
  private final String CHANNEL_PERM_PREFIX = "perm:v1:channel:";
  private final Duration DEFAULT_TTL = Duration.ofHours(1);

  @Override
  public long getServerPermission(UUID userId, Long serverId, Supplier<Long> dbFallback) {
    if (userId == null || serverId == null) return 0L;

    String key = SERVER_PERM_PREFIX + serverId + ":" +  userId;

    Long cacheMask = provider.get(key, DEFAULT_TTL, Long.class, dbFallback);

    return  cacheMask != null ? cacheMask : 0L; 
  }

  @Override
  public long getChannelPermission(UUID userId, Long serverId, Long channelId, Supplier<Long> dbFallback) {
    if (userId == null || serverId == null || channelId == null) return 0L;

    String key = CHANNEL_PERM_PREFIX + serverId + ":" + userId + ":" + channelId;

    Long cacheMask = provider.get(key, DEFAULT_TTL, Long.class, dbFallback);

    return cacheMask != null ? cacheMask : 0L;
  }

  @Override
  public void evictUserServerPermission(Long serverId, UUID userId) {
    if (serverId == null || userId == null) return;

    String serverKey = SERVER_PERM_PREFIX + serverId + ":" + userId;
    provider.evict(serverKey);

    String channelKey = CHANNEL_PERM_PREFIX + serverId + ":" + userId + ":";
    provider.evictByPrefix(channelKey);
  }

  @Override
  public void evictServerPermissionAll(Long serverId) {
    if (serverId == null) return;

    String key = SERVER_PERM_PREFIX + serverId + ":";

    provider.evictByPrefix(key);
  }

  @Override
  public void evictChannelPermissionsAll(Long serverId) {
    if (serverId == null) return;

    String key = CHANNEL_PERM_PREFIX + serverId + ":";

    provider.evictByPrefix(key);
  }
}