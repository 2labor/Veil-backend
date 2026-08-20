package com._labor.fakecord.infrastructure.cache.services.Impl;

import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.cache.CacheProvider;
import com._labor.fakecord.infrastructure.cache.Dto.ServerCacheDto;
import com._labor.fakecord.infrastructure.cache.services.ServerCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerCacheImpl implements ServerCache {
  private final CacheProvider cacheProvider;

  private final String CACHE_KEY_PREFIX = "server:v1:meta:";
  private final Duration TTL = Duration.ofHours(24);

  @Override
  public ServerCacheDto get(Long serverId, Supplier<ServerCacheDto> dbFallback) {
    String key = buildKey(serverId);
    return cacheProvider.get(key, TTL, ServerCacheDto.class, dbFallback);
  }

  @Override
  public void put(ServerCacheDto dto) {
    if (dto == null || dto.getId() == null) return;
    String key = buildKey(dto.getId());
    cacheProvider.set(key, dto, TTL);
  }

  @Override
  public void evict(Long serverId) {
    String key = buildKey(serverId);
    cacheProvider.evict(key);
  }
  
  private String buildKey(Long serverId) {
    return CACHE_KEY_PREFIX + serverId;
  }

}
