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
public class BlockCacheEvictor implements CacheEvictor{

  private final CacheVersionService versionService;
  private final CacheProvider cacheProvider;

  @Override
  public List<CacheType> support(String name) {
    return List.of(
      CacheType.BLOCKED_BY,
      CacheType.BLOCKED_LIST,
      CacheType.BLOCKS,
      CacheType.ALL
    );
  }

  @Override
  public void evict(CacheEvictEvent event) {
    UUID userId = event.aggregateId();
    String uId = userId.toString();

    versionService.incrementVersion(CacheType.BLOCKS.getName(), userId);
    versionService.incrementVersion(CacheType.BLOCKED_LIST.getName(), userId);

    cacheProvider.evictByPrefix(CacheType.BLOCKS.getPrefix() + uId);
    cacheProvider.evictByPrefix(CacheType.BLOCKED_BY.getPrefix() + uId);
    cacheProvider.evictByPrefix(CacheType.BLOCKED_LIST.getPrefix() + uId);

    log.info("Block-related caches cleared safely for user: {}", userId);
  }

}
