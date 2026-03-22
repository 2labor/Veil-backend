package com._labor.fakecord.services;

import java.util.UUID;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;

import com._labor.fakecord.domain.dto.UserProfileShort;


public interface FriendRequestQueryService {
  Slice<UserProfileShort> getIncomingRequests(UUID userId, Pageable pageable);
  Slice<UserProfileShort> getOutgoingRequests(UUID userId, Pageable pageable);
  long getCounterIncomingRequests(UUID userId);
  long getCounterOutgoingRequests(UUID userId);
}
