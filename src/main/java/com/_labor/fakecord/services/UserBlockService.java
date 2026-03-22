package com._labor.fakecord.services;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.dto.UserProfileShort;

public interface UserBlockService {
  void blockUser(UUID actorId, UUID targetId);
  void unblockUser(UUID actorId, UUID targetId);
  boolean isBlocked(UUID actorId, UUID targetId);
  Slice<UUID> getBlockedIds(UUID actorId, Pageable pageable);
  boolean isBlockedBy(UUID targetId, UUID actorId);
  Slice<UserProfileShort> getBlockedUsersList(UUID userId, Pageable pageable);
}
