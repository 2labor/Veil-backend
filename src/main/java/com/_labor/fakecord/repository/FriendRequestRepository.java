package com._labor.fakecord.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.FriendRequest;
import com._labor.fakecord.domain.enums.RequestStatus;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID>{
  Optional<FriendRequest> findBySenderIdAndTargetId(UUID userId, UUID targetId);
  List<FriendRequest> findByTargetIdAndStatus(UUID targetId, RequestStatus status);
  List<FriendRequest> findBySenderAndStatus(UUID senderId, RequestStatus status);
}
