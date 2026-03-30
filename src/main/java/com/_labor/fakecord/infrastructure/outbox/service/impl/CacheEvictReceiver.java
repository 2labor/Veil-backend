package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.cache.services.CacheVersionService;
import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;
import com._labor.fakecord.infrastructure.outbox.service.CacheEvictor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheEvictReceiver {
 
  private final List<CacheEvictor> evictors;
  private final CacheVersionService versionService;
  private final Map<CacheType, CacheEvictor> evictorMap = new EnumMap<>(CacheType.class);
  
  @PostConstruct
  public void init() {
    for (CacheEvictor evictor : evictors) {
      evictor.support("").forEach(type -> {
        evictorMap.put(type, evictor);
        log.info("Registered evictor {} for cache type: {}", evictor.getClass().getSimpleName(), type);
      });
    }
  }

  public void handleEvict(CacheEvictEvent event) {
    log.info("Eviction signal: User={}, Type={}", event.aggregateId(), event.cacheName());

    try {
      CacheType type = CacheType.fromString(event.cacheName());
      
      versionService.evictLocal(type.getName(), event.aggregateId());

      if (type == CacheType.ALL) {
        evictorMap.get(CacheType.ALL).evict(event);
        return;
      } 

      CacheEvictor evictor = evictorMap.get(type);
      if (evictor != null) {
        evictor.evict(event);
      }
    } catch (Exception e) {
      log.error("Failed to process eviction for event: {}", event, e);
    }
  }
}
