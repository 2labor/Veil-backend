package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.cache.CacheProvider;
import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
import com._labor.fakecord.infrastructure.outbox.service.CacheEvictor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendCacheEvictor implements CacheEvictor {

  private final CacheVersionService versionService;
  private final CacheProvider cacheProvider;

  @Override
  public List<CacheType> support(String name) {
    return List.of(CacheType.FRIENDS, CacheType.ALL);
  }

  @Override
  public void evict(CacheEvictEvent event) {
    UUID userId = event.aggregateId(); 
    String namespace = CacheType.FRIENDS.getName();
    String prefix = CacheType.FRIENDS.getPrefix();

    Long newVersion = versionService.incrementVersion(namespace, userId);

    cacheProvider.evictByPrefix(prefix + userId);

    log.info("Friend cache version bumped to {} and data cleared for user: {}", newVersion, userId); 
  }
}
