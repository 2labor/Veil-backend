package com._labor.fakecord.infrastructure.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheProvider {
  
  private final RedisTemplate<String, Object> redisTemplate;
  private final Cache<String, Object> localCache;
  private final ObjectMapper objectMapper;
  private final CacheVersionService versionService;

  public <T> T get(String key, Duration ttl, Class<T> clazz, Supplier<T> dbFallback) {
    Object local = localCache.getIfPresent(key);
    if (local != null) {
      return clazz.cast(local);
    }

    return clazz.cast(localCache.get(key, k -> {
      Object redis = redisTemplate.opsForValue().get(k);
      if (redis != null) {
        log.trace("L2 Cache hit for key: {}", k);
        return redis;
      }

      log.debug("L1/L2 Cache miss. Fetching from DB for key: {}", k);

      T db = dbFallback.get();
      if (db != null) {
        redisTemplate.opsForValue().set(k, db, ttl);
      }

      return db;
    }));
  }

  public void evict(String key) {
    log.debug("Evicting key: {}", key);
    localCache.invalidate(key);
    redisTemplate.delete(key);
  }

  public void evictByPrefix(String prefix) {
    log.info("Mass evicting for prefix: {}", prefix);

    localCache.asMap().keySet().removeIf(k -> k.startsWith(prefix));

    redisTemplate.execute((RedisCallback<Void>) connection -> {
      ScanOptions options = ScanOptions.scanOptions()
        .match(prefix + "*")
        .count(100)
        .build();

      try (var cursor = connection.keyCommands().scan(options)) {
        while (cursor.hasNext()) {
          byte[] key = cursor.next();
          connection.keyCommands().del(key); 
        }
      } catch (Exception e) {
        log.error("Error during Redis SCAN for prefix: {}", prefix, e);
      }
      return null;
    });
  }

  public <T> T getFromHash(String key, Class<T> clazz) {
    Map<Object, Object> entities = redisTemplate.opsForHash().entries(key);
    if (entities.isEmpty()) return null;
    return objectMapper.convertValue(entities, clazz);
  }

  public <T> T getVersioned(String basePrefix, UUID userId, Duration ttl, Class<T> clazz, Supplier<T> dbFallback) {
    long version = versionService.getVersion(basePrefix, userId);
    String versionKey = basePrefix + userId + ":v:" + version;
    return get(versionKey, ttl, clazz, dbFallback);
  }

  public <T> Map<String, T> getAll(List<String> keys, Class<T> clazz) {
    if (keys == null || keys.isEmpty()) return Map.of();

    Map<String, T> res = new HashMap<>();
    List<String> missingL1 = new ArrayList<>();

    for (String k : keys) {
      Object local = localCache.getIfPresent(k);
      if (local != null) {
        res.put(k, clazz.cast(local));
      } else {
        missingL1.add(k);
      }
    }

    if (missingL1.isEmpty()) return res;

    List<Object> redisValues = redisTemplate.opsForValue().multiGet(missingL1);
    for (int i = 0; i < missingL1.size(); i++) {
      String key = missingL1.get(i);
      Object redisValue = (i < redisValues.size()) ? redisValues.get(i) : null;
      if (redisValue != null) {
        T casted = clazz.cast(redisValue);
        res.put(key, casted);

        localCache.put(key, casted);
      }
    }

    return res;
  }

  public <T> void set(String key, T value, Duration ttl) {
    if (value == null) return;

    localCache.put(key, value);
    if (ttl != null) {
      redisTemplate.opsForValue().set(key, value, ttl);
    } else {
      redisTemplate.opsForValue().set(key, value);
    }
  }
}
