package com._labor.fakecord.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.entity.Channel;

import io.lettuce.core.dynamic.annotation.Param;

public interface ChannelRepository extends JpaRepository<Channel, Long>{
  List<Channel> findAllByServerIdOrderByPositionAsc(Long serverId);

  @Modifying
  @Query("UPDATE Channel c SET c.position = :position WHERE c.id = :id")
  void updatePosition(Long id, Integer position);

  @Modifying
  @Query("UPDATE Channel c SET c.lastActivityAt = :lastActivity WHERE c.id = :id")
  void updateLastActivity(Long id, Instant lastActivity);

  @Query("SELECT c FROM Channel c " +
    "JOIN ChannelMember cm ON c.id = cm.id.channelId " +
    "WHERE cm.id.userId = :userId " +
    "AND (c.type = com._labor.fakecord.domain.enums.ChannelType.DM " + 
    "OR c.type = com._labor.fakecord.domain.enums.ChannelType.GROUP_DM) " +
    "ORDER BY c.lastActivityAt DESC")
  Slice<Channel> findActiveDirectMessages(@Param("userId") UUID userId, Pageable pageable);

  @Query("""
    SELECT c FROM Channel c 
    WHERE c.type = 'DM' 
    AND EXISTS (SELECT 1 FROM ChannelMember cm1 WHERE cm1.id.channelId = c.id AND cm1.id.userId = :userA) 
    AND EXISTS (SELECT 1 FROM ChannelMember cm2 WHERE cm2.id.channelId = c.id AND cm2.id.userId = :userB)
  """)
  Optional<Channel> findExistingDM(UUID userA, UUID userB);

  @Modifying
  @Query("UPDATE Channel c SET c.lastMessageId = :msgId, c.lastActivityAt = :now, c.lastMessageContent = :preview WHERE c.id = :id")
  void updateChannelActivity(Long id, Long msgId, Instant now, String preview);
}
