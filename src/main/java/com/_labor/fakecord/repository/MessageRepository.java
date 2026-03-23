package com._labor.fakecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com._labor.fakecord.domain.dto.UnreadCountDto;
import com._labor.fakecord.domain.entity.Message;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>{
  @Modifying
  boolean existsByNonce(String none);
  Slice<Message> findAllByChannelIdOrderByIdDesc(Long channelId, Pageable pageable);
  Slice<Message> findAllByChannelIdAndIdLessThanOrderByIdDesc(Long channelId, Long messageId, Pageable pageable);
  Slice<Message> findAllByChannelIdAndIdGreaterThanOrderByIdAsc(Long channelId, Long targetId, Pageable pageable);
  long countByChannelIdAndIdGreaterThan(Long channelId, Long lastReadId); 
  @Modifying
  void deleteAllByChannelId(Long channelId);

  @Query("""
    SELECT new com._labor.fakecord.domain.dto.UnreadCountDto(m.channelId, COUNT(m.id))
    FROM Message m
    JOIN ChannelMember cm ON m.channelId = cm.id.channelId
    WHERE cm.id.userId = :userId
      AND m.channelId IN :channelIds
      AND m.id > cm.lastReadMessageId
    GROUP BY m.channelId
  """)
  List<UnreadCountDto> countUnreadForChannels(
    @Param("userId") UUID userId, 
    @Param("channelIds") List<Long> channelIds
  );
}