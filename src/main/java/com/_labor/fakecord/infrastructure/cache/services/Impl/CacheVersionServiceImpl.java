package com._labor.fakecord.infrastructure.cache.services.Impl;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheVersionServiceImpl implements CacheVersionService {

  private final StringRedisTemplate redisTemplate;

  private final Cache<String, Long> versionLocalCache = Caffeine.newBuilder()
    .expireAfterWrite(1, TimeUnit.MINUTES)
    .maximumSize(10_000)
    .build();

  @Override
  public long getVersion(String namespace, UUID userId) {
    String key = buildKey(namespace, userId);

    return versionLocalCache.get(key, k -> {
      log.trace("Version L1 miss for key: {}", k);
      String version = redisTemplate.opsForValue().get(k);
      return (version == null) ? 0L : Long.parseLong(version);
    });
  }

  @Override
  public long incrementVersion(String namespace, UUID userId) {
    String key = buildKey(namespace, userId);

    Long newVersion = redisTemplate.opsForValue().increment(key);
    long result = (newVersion == null) ? 1L : newVersion;

    versionLocalCache.put(key, result);

    log.debug("Version incremented in Redis to {} for key: {}", result, key);
    return result;
  }
  
  @Override
  public void evictLocal(String namespace, UUID userId) {
    String key = buildKey(namespace, userId);
    versionLocalCache.invalidate(key);
    log.trace("Local version cache invalidated for key: {}", key);
  }

  private String buildKey(String namespace, UUID userId) {
    return namespace + ":version:" + userId;
  }

}
