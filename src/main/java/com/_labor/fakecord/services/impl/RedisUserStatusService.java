package com._labor.fakecord.services.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.events.UserStatusChangedEvent;
import com._labor.fakecord.infrastructure.presence.PresenceMask;
import com._labor.fakecord.repository.UserProfileRepository;
import com._labor.fakecord.services.UserStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisUserStatusService implements UserStatusService {
  
  private final StringRedisTemplate redisTemplate;
  private final ApplicationEventPublisher eventPublisher;
  private final UserProfileRepository repository;

  private static final String STATUS_KEY_PREFIX = "status:";
  private static final Duration TTL_DEFAULT = Duration.ofMinutes(5);

  @Override
  public void setOnline(UUID userId) {
    int defaultMask = PresenceMask.createMask(UserStatus.ONLINE, false);
    updateMask(userId, defaultMask);
  }

  @Override
  public void setOffline(UUID userId) {
    redisTemplate.delete(STATUS_KEY_PREFIX + userId);
    eventPublisher.publishEvent(new UserStatusChangedEvent(userId, -1, true));
    log.debug("User {} manual logout/disconnect", userId);
  }

  @Override
  public boolean isOnline(UUID userId) {
    return Boolean.TRUE.equals(redisTemplate.hasKey(STATUS_KEY_PREFIX + userId));
  }
  
  @Override
  public void updateMask(UUID userId, int mask) {
    String cacheKey = STATUS_KEY_PREFIX + userId;

    redisTemplate.opsForValue().set(cacheKey, String.valueOf(mask), TTL_DEFAULT);
    eventPublisher.publishEvent(new UserStatusChangedEvent(userId, mask, false));
    log.trace("Presence mask heartbeated for user {}: {}", userId, mask);
  }

  @Override
  public int getMask(UUID userId) {
    String val = redisTemplate.opsForValue().get(STATUS_KEY_PREFIX + userId);
    return (val != null) ? Integer.parseInt(val) : -1;
  }

  @Override
  public void touch(UUID userId) {
    String cacheKey = STATUS_KEY_PREFIX + userId;
    redisTemplate.expire(cacheKey, TTL_DEFAULT);
  }

  @Override
  public void setOnlineWithPreference(UUID userId) {
    int currentMask = getMask(userId);
    if (currentMask != -1) {
      touch(userId);
      return;
    }

    repository.findById(userId).ifPresentOrElse(profile  -> {
      UserStatus pref = profile.getStatusPreference();
      if (pref == null) pref = UserStatus.ONLINE;
      boolean invisible = pref == UserStatus.INVISIBLE;
      int mask = PresenceMask.createMask(invisible ? UserStatus.INVISIBLE : pref, invisible);
      updateMask(userId, mask);
    }, () -> {
      updateMask(userId, PresenceMask.createMask(UserStatus.ONLINE, false));
    });
  }
    
}
