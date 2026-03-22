package com._labor.fakecord.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.FriendRequest;
import com._labor.fakecord.domain.enums.RequestStatus;

import jakarta.transaction.Transactional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID>{
  Optional<FriendRequest> findBySenderIdAndTargetId(UUID userId, UUID targetId);
  List<FriendRequest> findByTargetIdAndStatus(UUID targetId, RequestStatus status);
  List<FriendRequest> findBySenderIdAndStatus(UUID senderId, RequestStatus status);

  @Query("""
    SELECT new com._labor.fakecord.domain.dto.UserProfileShort(
      p.id, 
      p.displayName, 
      p.handle, 
      p.discriminator, 
      p.globalId, 
      p.avatarUrl, 
      p.statusPreference
    )
    FROM UserProfile p
    JOIN FriendRequest fr ON p.user.id = fr.sender.id
    WHERE fr.target.id = :userId AND fr.status = :status
  """)
  Slice<UserProfileShort> findAllIncomingShort(@Param("userId") UUID userId, @Param("status") RequestStatus status, Pageable pageable);

  @Query("""
    SELECT new com._labor.fakecord.domain.dto.UserProfileShort(
      p.id, 
      p.displayName, 
      p.handle, 
      p.discriminator, 
      p.globalId, 
      p.avatarUrl, 
      p.statusPreference
    )
    FROM UserProfile p
    JOIN FriendRequest fr ON p.user.id = fr.target.id
    WHERE fr.sender.id = :userId AND fr.status = :status
  """)
  Slice<UserProfileShort> findAllOutgoingShort(@Param("userId") UUID userId, @Param("status") RequestStatus status, Pageable pageable);
  
  @Query("SELECT COUNT(fr) FROM FriendRequest fr WHERE fr.target.id = :userId AND fr.status = 'PENDING'")
  long countIncomingRequests(UUID userId);

  @Query("SELECT COUNT(fr) FROM FriendRequest fr WHERE fr.sender.id = :userId AND fr.status = 'PENDING'")
  long countOutgoingRequests(UUID userId);

  @Modifying
  @Transactional
  @Query("""
    DELETE FROM FriendRequest r 
    WHERE (r.sender.id = :userA AND r.target.id = :userB) 
      OR (r.sender.id = :userB AND r.target.id = :userA)
  """)
  void deleteBetweenUsers(@Param("userA") UUID userA, @Param("userB") UUID userB);
}
