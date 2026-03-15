package com._labor.fakecord.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Relationships;
import com._labor.fakecord.domain.enums.RelationshipStatus;

import org.springframework.data.repository.query.Param;

public interface RelationshipRepository extends JpaRepository<Relationships, UUID> {
  Optional<Relationships> findByUserIdAndTargetId(UUID userId, UUID targetId);
  List<Relationships> findByUserIdAndStatus(UUID userId, RelationshipStatus status);
  void deleteByUserIdAndTargetIdOrUserIdAndTargetId(UUID u1, UUID t1, UUID u2, UUID t2);

  @Query("""
    SELECT new com._labor.fakecord.domain.dto.UserProfileShort(
      p.id, p.displayName, p.avatarUrl, p.statusPreference
    )
    FROM UserProfile p
    JOIN Relationships r ON p.id = r.target.id
    WHERE r.user.id = :userId AND r.status = :status
    ORDER BY p.displayName ASC
    """)
  Slice<UserProfileShort> findAllFriendsShort(@Param("userId") UUID userId, @Param("status") RelationshipStatus status, Pageable pageable);
  
  @Query("""
    SELECT new com._labor.fakecord.domain.dto.UserProfileShort(
      p.id, p.displayName, p.avatarUrl, null
    )
    FROM UserProfile p
    JOIN Relationships r1 ON p.id = r1.target.id
    JOIN Relationships r2 ON p.id = r2.target.id
    WHERE r1.user.id = :userA
      AND r2.user.id = :userB
      AND r1.status = com._labor.fakecord.domain.enums.RelationshipStatus.FRIENDS
      AND r2.status = com._labor.fakecord.domain.enums.RelationshipStatus.FRIENDS
  """)
  List<UserProfileShort> findMutualFriends(@Param("userA") UUID userA, @Param("userB") UUID userB);

  @Query("""
    SELECT COUNT(p.id)
    FROM UserProfile p
    JOIN Relationships r1 ON p.id = r1.target.id
    JOIN Relationships r2 ON p.id = r2.target.id
    WHERE r1.user.id = :userA 
      AND r2.user.id = :userB 
      AND r1.status = com._labor.fakecord.domain.enums.RelationshipStatus.FRIENDS
      AND r2.status = com._labor.fakecord.domain.enums.RelationshipStatus.FRIENDS
  """)
  long countMutualFriends(@Param("userA") UUID userA, @Param("userB") UUID userB);

  @Query("SELECT COUNT(r) > 0 FROM Relationships r WHERE " +
    "((r.user.id = :userA AND r.target.id = :userB) OR " +
    "(r.user.id = :userB AND r.target.id = :userA)) AND " +
    "r.status = :status")
  boolean existsByUsersAndStatus(UUID userA, UUID userB, RelationshipStatus status);
}