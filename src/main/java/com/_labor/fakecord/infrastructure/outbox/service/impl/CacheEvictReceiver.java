package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

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
    log.info("Received eviction signal from Redis: User={}, Type={}, SubType={}", event.aggregateId(), event.cacheName(), event.subType());

    try {
      CacheType type = CacheType.fromString(event.cacheName());
      
      if (type == CacheType.ALL) {
        evictors.stream()
        .filter(e -> e.support("").contains(CacheType.ALL))
        .forEach(e -> e.evict(event));
        return;
      }

      CacheEvictor evictor = evictorMap.get(type);
      if (evictor != null) {
        evictor.evict(event);
      } else {
        log.warn("No evictor found for cache type: {}", event.cacheName());
      }
    } catch (Exception e) {
      log.error("Error during cache eviction execution for event: {}", event, e);
    }
  }
}
