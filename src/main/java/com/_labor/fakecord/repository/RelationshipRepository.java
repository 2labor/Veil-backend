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
      p.id, 
      p.displayName, 
      p.avatarUrl, 
      p.statusPreference
    )
    FROM Relationships r
    JOIN r.target t
    JOIN t.userProfile p
    WHERE r.user.id = :userId AND r.status = :status
    ORDER BY p.displayName ASC
    """)
  Slice<UserProfileShort> findAllFriendsShort(@Param("userId") UUID userId, @Param("status") RelationshipStatus status, Pageable pageable); 
}