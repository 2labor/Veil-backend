package com._labor.fakecord.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.FriendRequest;
import com._labor.fakecord.domain.enums.RequestStatus;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID>{
  Optional<FriendRequest> findBySenderIdAndTargetId(UUID userId, UUID targetId);
  List<FriendRequest> findByTargetIdAndStatus(UUID targetId, RequestStatus status);
  List<FriendRequest> findBySenderAndStatus(UUID senderId, RequestStatus status);

  @Query("""
    SELECT new com._labor.fakecord.domain.dto.UserProfileShort(
      p.id, p.displayName, p.avatarUrl, p.statusPreference
    )
    FROM FriendRequest fr
    JOIN fr.sender s
    JOIN s.userProfile p
    WHERE fr.target.id = :userId AND fr.status = :status
  """)
  Slice<UserProfileShort> findAllIncomingShort(@Param("userId") UUID userId, @Param("status") RequestStatus status, Pageable pageable);

  @Query("""
    SELECT new com._labor.fakecord.domain.dto.UserProfileShort(
      p.id, p.displayName, p.avatarUrl, p.statusPreference
    )
    FROM FriendRequest fr
    JOIN fr.target t
    JOIN t.userProfile p
    WHERE fr.sender.id = :userId AND fr.status = :status
  """)
  Slice<UserProfileShort> findAllOutgoingShort(@Param("userId") UUID userId, @Param("status") RequestStatus status, Pageable pageable);
}
