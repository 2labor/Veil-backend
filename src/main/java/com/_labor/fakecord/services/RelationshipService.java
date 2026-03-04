package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.enums.RelationshipStatus;

public interface RelationshipService {
  void createFriendship(UUID userA, UUID userB);
  void removeFriend(UUID userId, UUID friendId);
  void blockUser(UUID senderId, UUID targetId);
  void unblockUser(UUID senderId, UUID targetId);
  Slice<UserProfileShort> getFriendsList(UUID userId, Pageable pageable);
  Slice<UserProfileShort> getBlockedUsers(UUID userId, Pageable pageable);
  RelationshipStatus getRelationshipStatus(UUID userA, UUID userB);
  List<UserProfileShort> getMutualFriends(UUID userA, UUID userB);
  long getMutualFriendsCount(UUID userA, UUID userB);
}
