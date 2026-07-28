package com._labor.fakecord.infrastructure.cache;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.UserProfile;
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
  
  @Override
  public Map<UUID, UserProfileShort> getUserProfileShortBatch(List<UUID> userIds) {
    if (userIds == null || userIds.isEmpty()) return Map.of();

    Map<UUID, Integer> masks = statusService.getMasks(userIds);

    Map<UUID, UserProfileFullDto> cachedProfiles = getFullProfilesBatch(userIds);

    Map<UUID, UserProfileShort> result = new HashMap<>();
    for (UUID id : userIds) {
      UserProfileFullDto fullDto = cachedProfiles.get(id);
      if (fullDto == null) continue;

      int mask = masks.getOrDefault(id, -1);
      UserStatus effectiveStatus = PresenceMask.getEffectiveStatus(mask);

      UserProfileShort shortDto = mapper.toShortDto(fullDto, effectiveStatus);
      result.put(id, shortDto);
    }

    return result;
  }

  private Map<UUID, UserProfileFullDto> getFullProfilesBatch(List<UUID> userIds) {
    if (userIds == null || userIds.isEmpty()) return Map.of();

    Map<String, UUID> keyToUserId = userIds.stream().collect(
      Collectors.toMap(
        userId -> REDIS_PREFIX + userId,
        userId -> userId
      )
    );

    List<String> keys = new ArrayList<>(keyToUserId.keySet());

    Map<String, UserProfileFullDto> cacheValues = cacheProvider.getAll(keys, UserProfileFullDto.class);

    Map<UUID, UserProfileFullDto> res = new HashMap<>();
    List<UUID> missingUserIds = new ArrayList<>();
    for (Map.Entry<String, UUID> entry : keyToUserId.entrySet()) {
      String key = entry.getKey();
      UUID userId = entry.getValue();

      UserProfileFullDto cachedDto = cacheValues.get(key);
      if (cachedDto != null) {
        res.put(userId, cachedDto);
      } else {
        missingUserIds.add(userId);
      }
    }

    if (!missingUserIds.isEmpty()) {
      log.debug("L1/L2 Cache miss for {} users. Fetching from DB", missingUserIds.size());

      List<UserProfile> dbProfiles = repository.findAllByUserIdIn(missingUserIds);
      for (UserProfile entity : dbProfiles) {
        var conns = connectionRepository.findByUser(entity.getUser());
        UserProfileFullDto fullDto = mapper.toFullDto(entity, UserStatus.OFFLINE, conns);
        
        String cacheKey = REDIS_PREFIX + entity.getId();
        
        cacheProvider.set(cacheKey, fullDto, REDIS_TTL); 
        res.put(entity.getId(), fullDto);
      }
    }

    return res;
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
