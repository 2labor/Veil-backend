package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

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
public class BlockCacheEvictor implements CacheEvictor{

  private final CacheVersionService versionService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final Cache<String, Object> localCache;

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

    List<String> patterns = List.of(
      CacheType.BLOCKS.getPrefix() + uId + ":*",
      CacheType.BLOCKED_BY.getPrefix() + uId + ":*",
      CacheType.BLOCKED_LIST.getPrefix() + uId + ":*"
    );  

    patterns.forEach(pattern -> {
      Set<String> keys = redisTemplate.keys(pattern);
      if (keys != null) redisTemplate.delete(keys);  
    });

    localCache.asMap().keySet().removeIf(k -> 
      k.startsWith(CacheType.BLOCKS.getPrefix() + uId) ||
      k.startsWith(CacheType.BLOCKED_BY.getPrefix() + uId) ||
      k.startsWith(CacheType.BLOCKED_LIST.getPrefix() + uId)
    );
  }

}
