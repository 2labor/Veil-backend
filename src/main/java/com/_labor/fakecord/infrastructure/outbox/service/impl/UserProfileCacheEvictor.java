package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
import com._labor.fakecord.infrastructure.outbox.service.CacheEvictor;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileCacheEvictor implements CacheEvictor {

  private final UserProfileCache userCacheService;

  @Override
  public List<CacheType> support(String name) {
    return List.of(CacheType.USER_PROFILES);
  }

  @Override
  public void evict(CacheEvictEvent event) {
    userCacheService.evict(event.aggregateId());
  }
  
}
