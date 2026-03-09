package com._labor.fakecord.infrastructure.cache.services;

import java.util.UUID;

public interface CacheVersionService {
  long getVersion(UUID userId);
  long incrementVersion(UUID userId);
}
