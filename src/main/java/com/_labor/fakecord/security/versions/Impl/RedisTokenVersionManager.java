package com._labor.fakecord.security.versions.Impl;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.cache.CacheProvider;
import com._labor.fakecord.repository.UserRepository;
import com._labor.fakecord.security.versions.TokenVersionManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisTokenVersionManager implements TokenVersionManager {

  private final StringRedisTemplate redisTemplate;
  private final CacheProvider cacheProvider;

  private final UserRepository userRepository;
  private final Cache<UUID, Integer> localCache;
  
  private final static String KEY_PREFIX = "auth:v:";
  private final static Duration CACHE_TTL = Duration.ofHours(24);

  public RedisTokenVersionManager(StringRedisTemplate redisTemplate, UserRepository userRepository, CacheProvider cacheProvider) {
    this.redisTemplate = redisTemplate;
    this.userRepository = userRepository;
    this.cacheProvider = cacheProvider;

    this.localCache = Caffeine.newBuilder()
      .expireAfterWrite(1, TimeUnit.MINUTES)
      .maximumSize(10_000)
      .build();
  }

  @Override
  public int getCurrentVersion(UUID userId) {
    return cacheProvider.get(
      KEY_PREFIX + userId,
      CACHE_TTL,
      Integer.class, 
      () -> userRepository.findTokenVersionById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"))
    );
  }

  @Override
  public void evictVersion(UUID userId) {
    cacheProvider.evict(KEY_PREFIX + userId);
  }

  @Override
  public void updateRedisCache(UUID userId, int version) {
    log.debug("Manual version update for user {}", userId);
    cacheProvider.evict(KEY_PREFIX + userId);
  }
  
}
