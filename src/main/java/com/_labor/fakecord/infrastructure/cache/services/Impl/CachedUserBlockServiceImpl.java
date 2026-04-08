package com._labor.fakecord.infrastructure.cache.services.Impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.infrastructure.cache.Dto.CachedSlice;
import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.infrastructure.repository.SocialCacheRepository;
import com._labor.fakecord.services.UserBlockService;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Primary
@Service
@RequiredArgsConstructor
@Slf4j
public class CachedUserBlockServiceImpl implements UserBlockService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final Cache<String, Object> localCache;
  private final UserBlockService delegate;
  private final CacheVersionService versionService;
  private final SocialCacheRepository cacheRepository;

  @Value("${fakecord.friends-block.duration-minutes}")
  private long ttlRedis;

  @Override
  public void blockUser(UUID actorId, UUID targetId) {
    delegate.blockUser(actorId, targetId);
    versionService.incrementVersion("blocks", actorId); 
    versionService.incrementVersion("blocks", targetId);
    versionService.incrementVersion("blocked_list", actorId);
    log.info("User {} blocked target {}. Cache versions incremented.", actorId, targetId);
  }

  @Override
  public void unblockUser(UUID actorId, UUID targetId) {
    delegate.unblockUser(actorId, targetId);
    versionService.incrementVersion("blocks", actorId);
    versionService.incrementVersion("blocks", targetId);
    versionService.incrementVersion("blocked_list", actorId);
    log.info("User {} unblocked target {}. Cache versions incremented.", actorId, targetId);
  }

  @Override
  public boolean isBlocked(UUID actorId, UUID targetId) {
    return cacheRepository.isBlocked(actorId, targetId)
      .orElseGet(() -> {
        return delegate.isBlocked(actorId, targetId);
      });
  }

  @Override
  public Slice<UUID> getBlockedIds(UUID actorId, Pageable pageable) {
    return delegate.getBlockedIds(actorId, pageable);
  }

  @Override
  public boolean isBlockedBy(UUID targetId, UUID actorId) {
    return isBlocked(targetId, actorId);
  }

  @Override
  public Slice<UserProfileShort> getBlockedUsersList(UUID userId, Pageable pageable) {
    if (pageable.getPageNumber() != 0) {
      return delegate.getBlockedUsersList(userId, pageable);
    }

    long version = versionService.getVersion("blocked_list", userId);
    String cacheKey = String.format("blocked_list:%s:v:%d:p:0", userId, version);

    @SuppressWarnings("unchecked")    
    CachedSlice<UserProfileShort> cache = (CachedSlice<UserProfileShort>) localCache.getIfPresent(cacheKey);
    if (cache != null) {
      return convertToSlice(cache, pageable);
    }   

    @SuppressWarnings("unchecked")
    CachedSlice<UserProfileShort> redisCache = (CachedSlice<UserProfileShort>) redisTemplate.opsForValue().get(cacheKey);
    if (redisCache != null) {
      return convertToSlice(redisCache, pageable);
    }

    log.debug("Cache miss for getBlockedUsersList(userId={}, pageable={}). Fetching from DB.", userId, pageable);
    Slice<UserProfileShort> db = delegate.getBlockedUsersList(userId, pageable);

    CachedSlice<UserProfileShort> dto = new CachedSlice<>(
      db.getContent(), db.getNumber(), db.getSize(), db.hasNext()
    );

    redisTemplate.opsForValue().set(cacheKey, dto, ttlRedis);
    localCache.put(cacheKey, dto);

    return db;
  }
  
  private Slice<UserProfileShort> convertToSlice(CachedSlice<UserProfileShort> dto, Pageable pageable) {
    return new SliceImpl<>(dto.content(), pageable, dto.hasNext());
  }
}
