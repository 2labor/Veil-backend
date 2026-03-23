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
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheSubType;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
import com._labor.fakecord.infrastructure.outbox.service.CacheEvictor;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendRequestCacheEvictor implements CacheEvictor {

  private final CacheVersionService versionService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final Cache<String, CachedSlice<UserProfileShort>> sliceCache;
  private final Cache<String, Long> counterCache;

  @Override
  public List<CacheType> support(String name) {
    return List.of(
      CacheType.REQUESTS_INCOMING, 
      CacheType.REQUESTS_ONGOING, 
      CacheType.REQUEST_COUNTER, 
      CacheType.ALL
    );
  }

  @Override
  public void evict(CacheEvictEvent event) {
    UUID userId = event.aggregateId();
    CacheType type = CacheType.fromString(event.cacheName());

    if (event.subType() == CacheSubType.ALL || type == CacheType.ALL) {
      log.debug("Full FriendRequest eviction for user: {}", userId);
      evictCounter(userId, "incoming");
      evictCounter(userId, "outgoing");
      evictList(userId, CacheType.REQUESTS_INCOMING);
      evictList(userId, CacheType.REQUESTS_ONGOING);
      return;
    }
    
    if (type == CacheType.REQUEST_COUNTER) {
      if (event.subType().name().contains("INCOMING")) {
        evictCounter(userId, "incoming");
      } else if (event.subType().name().contains("OUTGOING")) {
        evictCounter(userId, "outgoing");
      }
    } else {
      CacheType direction = event.subType().name().contains("INCOMING") 
        ? CacheType.REQUESTS_INCOMING 
        : CacheType.REQUESTS_ONGOING;
      evictList(userId, direction);
    }
  }

  private void evictCounter(UUID userId, String direction) {
    String cacheKey = CacheType.REQUEST_COUNTER.getPrefix() + direction + ":counter" + userId;
    redisTemplate.delete(cacheKey);
    counterCache.invalidate(cacheKey);
    log.debug("Counter evicted: {}", cacheKey);
  }

  private void evictList(UUID userId, CacheType direction) {
    versionService.incrementVersion(direction.getName(), userId);
    String prefix = direction.getPrefix() + userId;
    
    Set<String> keys = redisTemplate.keys(prefix + "*");
    if (keys != null && !keys.isEmpty()) {
      redisTemplate.delete(keys);
    }
    sliceCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    log.debug("List cache evicted for prefix: {}", prefix);
  }

}
