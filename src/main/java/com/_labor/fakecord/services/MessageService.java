package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.MessageWindowDto;
import com._labor.fakecord.domain.dto.ReplyPreviewDto;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.MessageType;

public interface MessageService {
  //Actions
  Message sendMessage(Long channelId, UUID authorId, String content, String nonce, Long parentId);
  Message editMessage(Long messageId, UUID operantId, String newContent);
  void deleteMessage(Long messageId, UUID requestId);
  void purgeChannelHistory(Long channelId, UUID requestId);
  List<MessageDto> enrichMessagesBatch(List<Message> messages);
  ReplyPreviewDto getReplyPreviewForSingleMessage(Long parentId);
  //System
  Message sendSystemMessage(Long channelId, UUID authorId, MessageType type, String metadata);
  // Queries
  Slice<Message> getLatestMessages(Long channelId, UUID currentUserId,  int size);
  Slice<Message> getMessagesBefore(Long channelId, UUID currentUserId, Long lastMessageId, int size);
  MessageWindowDto getMessageContent(Long channelId, UUID currentUserId, Long targetMessageId, int limit);  
}
