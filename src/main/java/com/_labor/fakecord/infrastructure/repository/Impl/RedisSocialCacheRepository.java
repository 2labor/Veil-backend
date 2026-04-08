package com._labor.fakecord.infrastructure.repository.Impl;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.repository.SocialCacheRepository;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RedisSocialCacheRepository implements SocialCacheRepository {

  private final Cache<UUID, Set<UUID>> l1Cache;
  private final RedisTemplate<String, String> redisTemplate;
  private static final String KEY_PREFIX = "social:blocks:";

  public RedisSocialCacheRepository(RedisTemplate<String, String> redisTemplate, @Qualifier("socialBlockCache") Cache<UUID, Set<UUID>> l1Cache) {
    this.redisTemplate = redisTemplate;
    this.l1Cache = l1Cache;
  }

  @Override
  public Optional<Boolean> isBlocked(UUID actorId, UUID targetId) {
    Set<UUID> cacheL1 = l1Cache.getIfPresent(actorId);
    if (cacheL1 != null) {
      log.trace("L1 Cache hit for user blocks: {}", actorId);
      return Optional.of(cacheL1.contains(targetId));
    }

    String key = KEY_PREFIX + actorId;
    if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
      return Optional.empty();
    }

    boolean isMember = Boolean.TRUE.equals(
      redisTemplate.opsForSet().isMember(key, targetId.toString())
    );

    return Optional.of(isMember);
  }

  @Override
  public void fillBlockedCache(UUID userId, Set<UUID> blockedIds) {
    String key = KEY_PREFIX + userId;

    redisTemplate.delete(key);
    if (blockedIds.isEmpty()) {
      redisTemplate.opsForSet().add(key, "EMPTY_MARKER");
    } else {
      String[] ids = blockedIds.stream().map(UUID::toString).toArray(String[]::new);
      redisTemplate.opsForSet().add(key, ids);
    }
    redisTemplate.expire(key, Duration.ofDays(5));

    l1Cache.put(userId, blockedIds);
  }

  @Override
  public void evict(UUID userId) {
    redisTemplate.delete(KEY_PREFIX + userId);
    l1Cache.invalidate(userId);
    log.debug("Evicted block cache for user: {}", userId);
  }
  
}
