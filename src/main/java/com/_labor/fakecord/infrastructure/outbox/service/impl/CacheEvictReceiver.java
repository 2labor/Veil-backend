package com._labor.fakecord.infrastructure.outbox.service.impl;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
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
  private final Cache<String, ?> localCache;

  public void handleEvict(CacheEvictEvent event) {
    log.info("Received evict signal for aggregate: {}", event.aggregateId());

    switch(event.cacheName()) {
      case "user_profiles" -> {
        userProfileCache.evict(event.aggregateId());
        log.debug("L1/L2 eviction for user_profile: {}", event.aggregateId());
      } 
      case "friends" -> {
        versionService.incrementVersion(event.aggregateId());
        String prefix = "friends:" + event.aggregateId();
        localCache.asMap().keySet().removeIf(key -> key.startsWith(prefix));

        log.debug("Targeted L1 eviction and version increment for user: {}", event.aggregateId());
      }
      default -> log.warn("Unknown cache name received: {}", event.cacheName());
    }
  }
}
