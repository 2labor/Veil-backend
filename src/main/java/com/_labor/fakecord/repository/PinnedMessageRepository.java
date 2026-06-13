package com._labor.fakecord.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com._labor.fakecord.domain.entity.PinnedMessage;

@Repository
public interface PinnedMessageRepository extends JpaRepository<PinnedMessage, Long>{
  Slice<PinnedMessage> findAllByChannelIdOrderByPinnedAtDesc(Long channelId, Pageable pageable);
  Optional<PinnedMessage> findFirstByChannelIdOrderByPinnedAtDesc(Long channelId);
  Optional<PinnedMessage> findFirstByChannelIdAndPinnedAtLessThanOrderByPinnedAtDesc(Long channelId, Instant pinnedAt);
  Optional<PinnedMessage> findFirstByChannelIdAndPinnedAtGreaterThanOrderByPinnedAtAsc(Long channelId, Instant pinnedAt);
  long countByChannelId(Long channelId);
}