package com._labor.fakecord.services;

import java.util.UUID;

import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.dto.MessageDto;

public interface PinnedMessageService {
  void pinMessage(Long channelId, Long messageId, UUID operatorId);
  void unpinMessage(Long channelId, Long messageId, UUID operatorId);
  Slice<MessageDto> getPinnedMessages(Long channelId, UUID currentUserId, int limit);
  MessageDto getLatestPinnedMessage(Long channelId, UUID currentUserId);
  MessageDto getNextPinnedMessage(Long channelId, Long currentMessageId, UUID currentUserId, String direction);
}
