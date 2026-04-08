package com._labor.fakecord.infrastructure.repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SocialCacheRepository {
  Optional<Boolean> isBlocked(UUID actorId, UUID targetId);
  void fillBlockedCache(UUID userId, Set<UUID> blockedIds);
  void evict(UUID userId);
}
