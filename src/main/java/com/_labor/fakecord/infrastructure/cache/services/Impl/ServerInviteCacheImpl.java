package com._labor.fakecord.infrastructure.cache.services.Impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ServerInviteResponseDto;
import com._labor.fakecord.domain.mappper.ServerInviteMapper;
import com._labor.fakecord.infrastructure.cache.CacheProvider;
import com._labor.fakecord.infrastructure.cache.services.ServerInviteCache;
import com._labor.fakecord.repository.ServerInviteRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerInviteCacheImpl implements ServerInviteCache {

  private final CacheProvider cacheProvider;
  private final ServerInviteRepository repository;
  private final ServerInviteMapper mapper;

  private final String REDIS_PREFIX = "invite:v1:";
  private final Duration DEFAULT_TTL = Duration.ofHours(24);

  @Override
  public void put(ServerInviteResponseDto dto) {
    if (dto == null || dto.code() == null) return;

    String key = REDIS_PREFIX + dto.code();
    Duration ttl = calculateTtl(dto.expiredAt());

    cacheProvider.set(key, dto, ttl);
  }

  @Override
  public Optional<ServerInviteResponseDto> get(String code) {
    if (code == null || code.isBlank()) return Optional.empty();

    String clearCode = code.trim();
    String key = REDIS_PREFIX + clearCode;

    ServerInviteResponseDto cache = cacheProvider.get(key,
      DEFAULT_TTL,
      ServerInviteResponseDto.class,
      () -> fetchFromDp(code)
    );

    if (cache == null) return Optional.empty();

    if (checkForExpired(cache)) {
      evict(code);
      return Optional.empty();
    }

    return Optional.of(cache);
  }

  @Override
  public void evict(String code) {
    if (code != null && !code.isBlank()) {
      cacheProvider.evict(REDIS_PREFIX + code); 
    }
  }
  
  private Duration calculateTtl(Long expiredAtMillis) {
    if (expiredAtMillis == null) return null;
    long now = Instant.now().toEpochMilli();
    long diff = expiredAtMillis - now;
    return diff > 0 ? Duration.ofMillis(diff) : Duration.ofSeconds(1);
  }

  private ServerInviteResponseDto fetchFromDp(String code) {
    return repository.findById(code)
      .map(mapper::toDto)
      .orElse(null);
  }


  private boolean checkForExpired(ServerInviteResponseDto dto) {
    if (dto.expiredAt() != null && Instant.now().toEpochMilli() > dto.expiredAt()) {
        return true;
    }
    
    return dto.maxUsed() != null && dto.maxUsed() > 0 && dto.countUsed() >= dto.maxUsed();
  }
}
