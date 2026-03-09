package com._labor.fakecord.infrastructure.cache.services.Impl;

import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheVersionServiceImpl implements CacheVersionService {

  private final StringRedisTemplate redisTemplate;
  private static final String VERSION_PREFIX = "friends:version:";

  @Override
  public long getVersion(UUID userId) {
    String version = redisTemplate.opsForValue().get(VERSION_PREFIX + userId);
    return (version == null) ? 0 : Long.parseLong(version);
  }

  @Override
  public long incrementVersion(UUID userId) {
    Long newVersion = redisTemplate.opsForValue().increment(VERSION_PREFIX + userId);
    return (newVersion == null) ? 0 : newVersion;
  }
  
}
