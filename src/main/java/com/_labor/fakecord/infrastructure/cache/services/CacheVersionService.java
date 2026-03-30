package com._labor.fakecord.infrastructure.cache.services;

import java.util.UUID;

public interface CacheVersionService {
  long getVersion(String namespace, UUID userId);
  long incrementVersion(String namespace, UUID userId);
  void evictLocal(String namespace, UUID userId);
}
