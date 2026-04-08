package com._labor.fakecord.infrastructure.cache;

import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.infrastructure.presence.PresenceMask;
import com._labor.fakecord.repository.UserConnectionRepository;
import com._labor.fakecord.repository.UserProfileRepository;
import com._labor.fakecord.services.UserProfileCache;
import com._labor.fakecord.services.UserStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProfileCacheImpl implements UserProfileCache {

  private final UserProfileRepository repository;
  private final UserStatusService statusService;
  private final UserProfileMapper mapper;
  private final CacheProvider cacheProvider;
  private final UserConnectionRepository connectionRepository;

  private static final String REDIS_PREFIX = "profile:v1:";
  private static final Duration REDIS_TTL = Duration.ofHours(24);

  @Override
  public UserProfileFullDto getUserProfile(UUID userId) {
  
    UserProfileFullDto staticProfile = cacheProvider.get(
      REDIS_PREFIX + userId,
      REDIS_TTL,
      UserProfileFullDto.class,
      () -> fetchFromDb(userId)
    );

    int mask = statusService.getMask(userId);
    UserStatus effective = PresenceMask.getEffectiveStatus(mask);
    
    return staticProfile.toBuilder()
      .status(effective)
      .statusPreference(effective == UserStatus.OFFLINE ? UserStatus.OFFLINE : staticProfile.statusPreference())
      .build();
  }

  @Override
  public void evict(UUID userId) {
    cacheProvider.evict(REDIS_PREFIX + userId);
  }
  
  private UserProfileFullDto fetchFromDb(UUID userId) {
    return repository.findById(userId)
      .map(entity -> {
        var conns = connectionRepository.findByUser(entity.getUser());
        return mapper.toFullDto(entity, UserStatus.OFFLINE, conns);
      })
      .orElseGet(() -> createNegativeProfile(userId));
  }

  private UserProfileFullDto createNegativeProfile(UUID userId) {
    return UserProfileFullDto.builder()
      .userId(userId)
      .displayName("Deleted User")
      .bio("This profile does not exist")
      .isGhost(true)
      .build();
  }
}
