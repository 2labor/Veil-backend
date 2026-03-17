package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.infrastructure.cache.Dto.CachedSlice;
import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
import com._labor.fakecord.infrastructure.outbox.service.CacheEvictor;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendCacheEvictor implements CacheEvictor {

  private final CacheVersionService versionService;
  private final RedisTemplate<String, Object> redisTemplate;
    private final Cache<String, CachedSlice<UserProfileShort>> sliceCache;

  @Override
  public List<CacheType> support(String name) {
    return List.of(CacheType.FRIENDS, CacheType.ALL);
  }

  @Override
  public void evict(CacheEvictEvent event) {
    UUID userId = event.aggregateId();

    long newVersion = versionService.incrementVersion(CacheType.FRIENDS.getName(), userId);

    String pattern = CacheType.FRIENDS.getPrefix() + userId + ":v:" + (newVersion - 1) + ":*";
    Set<String> oldKeys = redisTemplate.keys(pattern);
    if (oldKeys != null) redisTemplate.delete(oldKeys);

    String prefix = CacheType.FRIENDS.getPrefix() + userId;
    sliceCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));

    log.info("Version incremented to {} and old keys cleared for {}", newVersion, userId);
  }
  
}
