package com._labor.fakecord.services;

import java.util.UUID;
import com._labor.fakecord.domain.enums.RequestSource;

public interface FriendRequestCommandService {
  void sendFriendRequest(UUID senderId, UUID targetId, RequestSource source);
  void acceptFriendRequest(UUID currentUser, UUID requesterId);
  void declineOrCancelRequest(UUID senderId, UUID targetId);
  void ignoreRequest(UUID currentUserId, UUID requesterId);
}
