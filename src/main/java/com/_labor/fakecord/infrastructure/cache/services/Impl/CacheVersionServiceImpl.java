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

  @Override
  public long getVersion(String namespace, UUID userId) {
    String version = redisTemplate.opsForValue().get(buildKey(namespace, userId));
    return (version == null) ? 0 : Long.parseLong(version);
  }

  @Override
  public long incrementVersion(String namespace, UUID userId) {
    Long newVersion = redisTemplate.opsForValue().increment(buildKey(namespace, userId));
    return (newVersion == null) ? 0 : newVersion;
  }
  

  private String buildKey(String namespace, UUID userId) {
    return namespace + ":version:" + userId;
  }
}
