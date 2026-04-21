package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
import com._labor.fakecord.infrastructure.outbox.service.CacheEvictor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class AllCacheEvictor implements CacheEvictor {

  private final FriendCacheEvictor friendCache;
  private final FriendRequestCacheEvictor requestsCache;
  private final BlockCacheEvictor blockCache;

  @Override
  public List<CacheType> support(String name) {
    return List.of(CacheType.ALL);
  }

  @Override
  public void evict(CacheEvictEvent event) {
    log.info("Social eviction processing for user: {} (Cache: {})", event.aggregateId(), event.cacheName());

    CacheType type = CacheType.fromString(event.cacheName());

    switch (type) {
      case FRIENDS -> {
        requestsCache.evict(event);
        friendCache.evict(event);
      }

      case BLOCKS -> {
        friendCache.evict(event);
        requestsCache.evict(event);
        blockCache.evict(event);
      }
      
      default -> {
        friendCache.evict(event);
        requestsCache.evict(event);
        blockCache.evict(event);
      }
    }
  }
  
}
