package com._labor.fakecord.services;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.enums.RelationshipStatus;

public interface RelationshipQueryService {
  Slice<UserProfileShort> getFriendsList(UUID userId, Pageable pageable);
  RelationshipStatus getRelationshipStatus(UUID userA, UUID userB);
  List<UserProfileShort> getMutualFriends(UUID userA, UUID userB);
  long getMutualFriendsCount(UUID userA, UUID userB);
}
