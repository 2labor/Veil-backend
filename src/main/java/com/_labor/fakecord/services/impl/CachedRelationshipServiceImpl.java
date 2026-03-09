package com._labor.fakecord.services.impl;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.enums.RelationshipStatus;
import com._labor.fakecord.infrastructure.cache.Dto.CachedSlice;
import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.services.RelationshipQueryService;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class CachedRelationshipServiceImpl implements RelationshipQueryService {

  private final RelationshipServiceImpl delegate;
  private final CacheVersionService versionService;
  private final RedisTemplate<String, Object> redisTemplate;
  private final Cache<String, CachedSlice<UserProfileShort>> localCache;

  @Value("${fakecord.friends-list.duration-minutes}")
  private long ttlRedis;
  
  @Override
  public Slice<UserProfileShort> getFriendsList(UUID userId, Pageable pageable) {
    if (pageable.getPageNumber() != 0) {
      return delegate.getFriendsList(userId, pageable);
    }

    long version = versionService.getVersion(userId);
    String cacheKey = String.format("friends:%s:v:%d:p:0", userId.toString(), version);

    CachedSlice<UserProfileShort> cache = localCache.getIfPresent(cacheKey);
    if (cache != null) return convertToSlice(cache, pageable);

    CachedSlice<UserProfileShort> redisCache = (CachedSlice<UserProfileShort>) redisTemplate.opsForValue().get(cacheKey);
    if (redisCache != null) {
      localCache.put(cacheKey, redisCache);
      return convertToSlice(redisCache, pageable);
    }  

    Slice<UserProfileShort> db = delegate.getFriendsList(userId, pageable);

    CachedSlice dto = new CachedSlice<>(
      db.getContent(), db.getNumber(), db.getSize(), db.hasNext()
    );

    redisTemplate.opsForValue().set(cacheKey, dto, Duration.ofMinutes(ttlRedis));
    localCache.put(cacheKey, dto);

    return db;
  }

  private Slice<UserProfileShort> convertToSlice(CachedSlice<UserProfileShort> dto, Pageable pageable) {
    return new SliceImpl<>(dto.content(), pageable, dto.hasNext());
  }

  @Override
  public Slice<UserProfileShort> getBlockedUsers(UUID userId, Pageable pageable) {
    return delegate.getBlockedUsers(userId, pageable);
  }

  @Override
  public RelationshipStatus getRelationshipStatus(UUID userA, UUID userB) {
    return delegate.getRelationshipStatus(userA, userB);
  }

  @Override
  public List<UserProfileShort> getMutualFriends(UUID userA, UUID userB) {
    return delegate.getMutualFriends(userA, userB);
  }

  @Override
  public long getMutualFriendsCount(UUID userA, UUID userB) {
    return delegate.getMutualFriendsCount(userA, userB);
  }
  
}
