package com._labor.fakecord.repository;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.UserBlock;
import org.springframework.data.repository.query.Param;

public interface UserBlockRepository extends JpaRepository<UserBlock, UUID> {
  boolean existsByUserIdAndTargetId(UUID userId, UUID targetId);

  @Query("SELECT ub.user.id FROM UserBlock ub WHERE ub.target.id = :userId")
  Slice<UUID> findWhoBlockedUserId(@Param("userId") UUID userId, Pageable pageable);

  @Query("SELECT ub.target.id FROM UserBlock ub WHERE ub.user.id = :userId")
  Slice<UUID> findBlockedIdsByUserId(@Param("userId") UUID userId, Pageable pageable);

  void deleteByUserIdAndTargetId(UUID userId, UUID targetId);

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
    FROM UserBlock ub
    JOIN ub.target t
    JOIN UserProfile p ON p.user.id = t.id
    WHERE ub.user.id = :userId
  """)
  Slice<UserProfileShort> findAllBlockedProfiles(@Param("userId") UUID userId, Pageable pageable);
}
