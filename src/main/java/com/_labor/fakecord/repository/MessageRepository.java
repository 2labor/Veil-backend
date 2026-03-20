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
  Slice<Message> findFirst50ByChannelIdOrderByIdDesc(Long chanelId);
  Slice<Message> findAllByChannelIdAndIdLessThanOrderByIdDesc(Long chanelId, Long messageId, Pageable pageable);
  
  @Modifying
  void deleteAllByChannelId(Long channelId);
}