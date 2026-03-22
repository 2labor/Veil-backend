package com._labor.fakecord.services;

import java.util.UUID;

public interface RelationshipCommandService {
  void createFriendship(UUID userA, UUID userB);
  void removeFriend(UUID userId, UUID friendId);
  void forceTerminateRelationships(UUID userA, UUID userB);
}
