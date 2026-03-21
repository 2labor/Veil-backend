package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.entity.Message;

public interface MessageService {
  Message sendMessage(Long channelId, UUID authorId, String content, String nonce);
  Slice<Message> getLatestMessages(Long channelId, int size);
  Slice<Message> getMessagesBefore(Long channelId, Long lastMessageId, int size);
  void deleteMessage(Long messageId, UUID requestId);
  void purgeChannelHistory(Long channelId, UUID requestId);
  List<Message> getMessageContent(Long chanelId, Long targetMessageId, int limit);
}
