package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.cache.CacheProvider;
import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheSubType;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
import com._labor.fakecord.infrastructure.outbox.service.CacheEvictor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FriendRequestCacheEvictor implements CacheEvictor {

  private final CacheVersionService versionService;
  private final CacheProvider cacheProvider;

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
      String dir = event.subType().name().contains("INCOMING") ? "incoming" : "outgoing";
      evictCounter(userId, dir);
    } else {
      CacheType direction = event.subType().name().contains("INCOMING") 
        ? CacheType.REQUESTS_INCOMING 
        : CacheType.REQUESTS_ONGOING;
      evictList(userId, direction);
    }
  }

  private void evictCounter(UUID userId, String direction) {
    String cacheKey = CacheType.REQUEST_COUNTER.getPrefix() + direction + ":counter" + userId;
    cacheProvider.evict(cacheKey);
    log.debug("Counter evicted: {}", cacheKey);
  }

  private void evictList(UUID userId, CacheType direction) {
    versionService.incrementVersion(direction.getName(), userId);

    String prefix = direction.getPrefix() + userId;
    cacheProvider.evictByPrefix(prefix);
    
    log.debug("List cache evicted for prefix: {}", prefix);
  }

}
