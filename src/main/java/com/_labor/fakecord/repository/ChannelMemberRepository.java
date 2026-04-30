package com._labor.fakecord.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.dto.MessageContext;
import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.domain.entity.ChannelMemberId;

import io.lettuce.core.dynamic.annotation.Param;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId> {

  Slice<ChannelMember> findAllById_ChannelId(Long channelId, Pageable pageable);

  boolean existsById_ChannelIdAndId_UserId(Long channelId, UUID userId);

  Optional<ChannelMember> findById_ChannelIdAndId_UserId(Long channelId, UUID userId);

  List<ChannelMember> findAllById_UserId(UUID userId);

  @Query("SELECT cm.id.userId FROM ChannelMember cm " +
    "WHERE cm.id.channelId = :channelId AND cm.id.userId IN :userIds")
  List<UUID> findAllUserIdsByChannelIdAndUserIdIn(
    @Param("channelId") Long channelId, 
    @Param("userIds") List<UUID> userIds
  );

  @Query("SELECT cm.id.userId FROM ChannelMember cm WHERE cm.id.channelId = :channelId")
  List<UUID> findAllUserIdsByChannelId(@Param("channelId") Long channelId);

  @Query("SELECT cm.id.userId FROM ChannelMember cm " +
    "WHERE cm.id.channelId = :channelId AND cm.id.userId <> :myId")
  Optional<UUID> findFirstRecipientId(@Param("channelId") Long channelId, @Param("myId") UUID myId);

  long countById_ChannelId(Long channelId);

  @Modifying
  @Query("DELETE FROM ChannelMember cm WHERE cm.id.channelId = :channelId AND cm.id.userId = :userId")
  void deleteById_ChannelIdAndId_UserId(@Param("channelId") Long channelId, @Param("userId") UUID userId); 

  @Modifying
  @Query("DELETE FROM ChannelMember cm WHERE cm.id.channelId = :channelId")
  void deleteAllByChannelId(@Param("channelId") Long channelId);

  @Modifying
  @Query("UPDATE ChannelMember cm SET cm.lastReadMessageId = :messageId " +
    "WHERE cm.id.channelId = :channelId AND cm.id.userId = :userId " +
    "AND (cm.lastReadMessageId IS NULL OR cm.lastReadMessageId < :messageId)")
  int updateLastReadIfGreater(@Param("channelId") Long channelId, 
    @Param("userId") UUID userId, 
    @Param("messageId") Long messageId);

  @Query(value = """
    SELECT 
      c.type as type, 
      c.name as name, 
      c.server_id as serverId, 
      (SELECT cm2.user_id 
      FROM channel_members cm2 
      WHERE cm2.channel_id = c.id AND cm2.user_id <> :userId 
      LIMIT 1) as partnerId 
    FROM channels c 
    JOIN channel_members cm1 ON c.id = cm1.channel_id 
    WHERE c.id = :channelId AND cm1.user_id = :userId
    """, nativeQuery = true)
  Optional<MessageContext> getMessageContext(@Param("channelId") Long channelId, @Param("userId") UUID userId);
}