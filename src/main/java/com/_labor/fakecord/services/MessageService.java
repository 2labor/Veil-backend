package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.MessageType;

public interface MessageService {
  //Actions
  Message sendMessage(Long channelId, UUID authorId, String content, String nonce);
  Message editMessage(Long messageId, UUID operantId, String newContent);
  void deleteMessage(Long messageId, UUID requestId);
  void purgeChannelHistory(Long channelId, UUID requestId);
  //System
  Message sendSystemMessage(Long channelId, UUID authorId, MessageType type, String metadata);
  // Queries
  Slice<Message> getLatestMessages(Long channelId, UUID currentUserId,  int size);
  Slice<Message> getMessagesBefore(Long channelId, UUID currentUserId, Long lastMessageId, int size);
  List<Message> getMessageContent(Long chanelId,  UUID currentUserId, Long targetMessageId, int limit);
}
