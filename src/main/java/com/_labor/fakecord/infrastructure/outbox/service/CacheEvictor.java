package com._labor.fakecord.infrastructure.outbox.service;

import java.util.List;

import com._labor.fakecord.infrastructure.outbox.domain.CacheEvictEvent;
import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheType;

public interface CacheEvictor {
  List<CacheType> support(String name);
  void evict(CacheEvictEvent event);
}
