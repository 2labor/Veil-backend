package com._labor.fakecord.repository;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com._labor.fakecord.domain.entity.MessageReaction;

@Repository
public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long>{
  Slice<MessageReaction> findByMessageIdAndEmojiId(Long messageId, Long emojiId, Pageable pageable);
  
  @Modifying
  @Query("DELETE FROM MessageReaction mr WHERE mr.messageId = :messageId AND mr.userId = :userId AND mr.emojiId = :emojiId")
  void deleteByMessageIdAndUserIdAndEmojiId(Long messageId, UUID userId, Long emojiId);
  
  @Modifying
  @Query("DELETE FROM MessageReaction mr WHERE mr.messageId = :messageId")
  void deleteByMessageId(Long messageId);
}
