package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.infrastructure.cache.Dto.CachedSlice;
import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheSubType;
import com._labor.fakecord.services.UserProfileCache;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheEvictReceiver {
  private final UserProfileCache userProfileCache;
  private final CacheVersionService versionService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final Cache<String, CachedSlice<UserProfileShort>> sliceCache;
  private final Cache<String, Long> counterCache;

  public void handleEvict(CacheEvictEvent event) {
    log.info("Received evict signal for aggregate: {}", event.aggregateId());

    switch(event.cacheName()) {
      case "user_profiles" -> {
        userProfileCache.evict(event.aggregateId());
        log.debug("L1/L2 eviction for user_profile: {}", event.aggregateId());
      } 
      case "friends" -> {
        long newVersion = versionService.incrementVersion("friends", event.aggregateId());

        String oldCacheKey = String.format("friends:%s:v:%d:*", event.aggregateId(), newVersion - 1);

        Set<String> oldKeys = redisTemplate.keys(oldCacheKey);
        if (oldKeys != null && !oldKeys.isEmpty()) {
          redisTemplate.delete(oldKeys);
        }
        
        String prefix = "friends:" + event.aggregateId();
        List<String> keys = sliceCache.asMap().keySet().stream()
        .filter(key -> key.startsWith(prefix))
        .toList();
        sliceCache.invalidateAll(keys);
        
        log.info("Version incremented to {} and old keys cleared for {}", newVersion, event.aggregateId());
      }

      case "request-list" -> {
        String namespace = (event.subType() == CacheSubType.INCOMING_LIST) ? "requests-incoming" : "requests-ongoing";

        versionService.incrementVersion(namespace, event.aggregateId());

        String oldCacheKey = String.format("%s:%s:v:*", namespace, event.aggregateId());
        
        Set<String> oldKeys = redisTemplate.keys(oldCacheKey);
        if (oldKeys != null && !oldKeys.isEmpty()) {
          redisTemplate.delete(oldKeys);
        }

        String prefix = namespace + ":" + event.aggregateId();
        List<String> keys = sliceCache.asMap().keySet().stream() 
          .filter(key -> key.startsWith(prefix))
          .toList();
        sliceCache.invalidateAll(keys);

      }

      case "request-counter" -> {
        String direction = (event.subType() == CacheSubType.INCOMING_COUNTER) ? "incoming" : "outgoing";
        String cacheKey = "request:" + direction + ":counter" + event.aggregateId();

        redisTemplate.delete(cacheKey);
        counterCache.invalidate(cacheKey);
      }
      default -> log.warn("Unknown cache name received: {}", event.cacheName());
    }
  }
}
