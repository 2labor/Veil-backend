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
import com._labor.fakecord.services.FriendRequestQueryService;
import com._labor.fakecord.services.impl.FriendRequestServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@Slf4j
@RequiredArgsConstructor
public class CachedFriendRequestServiceImpl implements  FriendRequestQueryService {

  private final FriendRequestServiceImpl delegate;
  private final RedisTemplate<String, Object> redisTemplate;
  private final Cache<String, CachedSlice<UserProfileShort>> sliceCache;
  private final Cache<String, Long> counterCache;
  private final CacheVersionService versionService;

  @Value("${fakecord.requests-incoming-counter.duration-minutes}")
  private Long counterTtl;

  @Value("${fakecord.friends-list.duration-minutes}")
  private Long redisRequestsTtl;

  @Override
  public Slice<UserProfileShort> getIncomingRequests(UUID userId, Pageable pageable) {
    if (pageable.getPageNumber() != 0) {
      return delegate.getIncomingRequests(userId, pageable);
    }

    long version = versionService.getVersion("requests-incoming", userId);
    String cacheKey = String.format("requests-incoming:%s:v:%d:p:0", userId, version);

    CachedSlice<UserProfileShort> localCache = sliceCache.getIfPresent(cacheKey);
    if (localCache != null) {
      return convertToSlice(localCache, pageable);
    }

    @SuppressWarnings("unchecked")
    CachedSlice<UserProfileShort> redisCache = (CachedSlice<UserProfileShort>) redisTemplate.opsForValue().get(cacheKey);
    if (redisCache != null) {
      sliceCache.put(cacheKey, redisCache);
      return convertToSlice(redisCache, pageable);
    }

    Slice<UserProfileShort> db = delegate.getIncomingRequests(userId, pageable);

    CachedSlice<UserProfileShort> dto = new CachedSlice<>(
      db.getContent(), db.getPageable().getPageNumber(), db.getPageable().getPageSize(), db.hasNext()
    );
    sliceCache.put(cacheKey, dto);
    redisTemplate.opsForValue().set(cacheKey, dto, redisRequestsTtl);

    return db;
  }

  @Override
  public Slice<UserProfileShort> getOutgoingRequests(UUID userId, Pageable pageable) {
    if (pageable.getPageNumber() != 0) {
      return delegate.getOutgoingRequests(userId, pageable);
    }

    long version = versionService.getVersion("requests-ongoing", userId);
    String cacheKey = String.format("requests-ongoing:%s:v:%d:p:0", userId, version);

    CachedSlice<UserProfileShort> localCache = sliceCache.getIfPresent(cacheKey);
    if (localCache != null) {
      return convertToSlice(localCache, pageable);
    }

    @SuppressWarnings("unchecked")
    CachedSlice<UserProfileShort> redisCache = (CachedSlice<UserProfileShort>) redisTemplate.opsForValue().get(cacheKey);
    if (redisCache != null) {
      sliceCache.put(cacheKey, redisCache);
      return convertToSlice(redisCache, pageable);
    }

    Slice<UserProfileShort> db = delegate.getOutgoingRequests(userId, pageable);

    CachedSlice<UserProfileShort> dto = new CachedSlice<>(
      db.getContent(), db.getPageable().getPageNumber(), db.getPageable().getPageSize(), db.hasNext()
    );
    sliceCache.put(cacheKey, dto);
    redisTemplate.opsForValue().set(cacheKey, dto, redisRequestsTtl);

    return db;
  }

  @Override
  public long getCounterIncomingRequests(UUID userId) {
    String cacheKey = "request:incoming:counter" + userId;

    Long local = counterCache.getIfPresent(cacheKey);
    if (local != null) {
      return local;
    }

    Long redisCache = (Long) redisTemplate.opsForValue().get(cacheKey);
    if (redisCache != null) {
      counterCache.put(cacheKey, redisCache);
      return redisCache;
    }

    Long dbResponse = delegate.getCounterIncomingRequests(userId);
    redisTemplate.opsForValue().set(cacheKey, dbResponse, counterTtl);
    counterCache.put(cacheKey, dbResponse);

    return dbResponse;
  }

  @Override
  public long getCounterOutgoingRequests(UUID userId) {
    String cacheKey = "request:outgoing:counter" + userId;

    Long local = counterCache.getIfPresent(cacheKey);
    if (local != null) {
      return local;
    }

    Long redisCache = (Long) redisTemplate.opsForValue().get(cacheKey);
    if (redisCache != null) {
      counterCache.put(cacheKey, redisCache);
      return redisCache;
    }

    Long dbResponse = delegate.getCounterOutgoingRequests(userId);
    redisTemplate.opsForValue().set(cacheKey, dbResponse, counterTtl);
    counterCache.put(cacheKey, dbResponse);

    return dbResponse;
  }

  private Slice<UserProfileShort> convertToSlice(CachedSlice<UserProfileShort> dto, Pageable pageable) {
    return new SliceImpl<>(dto.content(), pageable, dto.hasNext());
  }
}
