package com._labor.fakecord.services;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.enums.RequestSource;

public interface FriendRequestService {
  void sendFriendRequest(UUID senderId, UUID targetId, RequestSource source);
  void acceptFriendRequest(UUID currentUser, UUID requesterId);
  void declineOrCancelRequest(UUID senderId, UUID targetId);
  void ignoreRequest(UUID currentUserId, UUID requesterId);
  Slice<UserProfileShort> getIncomingRequests(UUID userId, Pageable pageable);
  Slice<UserProfileShort> getOutgoingRequests(UUID userId, Pageable pageable);
  long getCounterIncomingRequests(UUID userId);
  long getCounterOutgoingRequests(UUID userId);
}
