package com._labor.fakecord.repository;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com._labor.fakecord.domain.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>{
  @Modifying
  boolean existsByNonce(String none);
  Slice<Message> findAllByChannelIdOrderByIdDesc(Long channelId, Pageable pageable);
  Slice<Message> findAllByChannelIdAndIdLessThanOrderByIdDesc(Long channelId, Long messageId, Pageable pageable);
  Slice<Message> findAllByChannelIdAndIdGreaterThanOrderByIdAsc(Long channelId, Long targetId, Pageable pageable);
  @Modifying
  void deleteAllByChannelId(Long channelId);
}