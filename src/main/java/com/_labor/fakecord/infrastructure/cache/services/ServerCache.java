package com._labor.fakecord.infrastructure.cache.services;

import java.util.function.Supplier;

import com._labor.fakecord.infrastructure.cache.Dto.ServerCacheDto;

public interface ServerCache {
  ServerCacheDto get(Long serverId, Supplier<ServerCacheDto> dbFallback);
  void put(ServerCacheDto dto);
  void evict(Long serverId);
}