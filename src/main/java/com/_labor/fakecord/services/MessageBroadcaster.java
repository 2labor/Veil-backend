package com._labor.fakecord.services;

import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.SocketEventType;

public interface MessageBroadcaster {
  void broadcastMessageEvent(Message message, SocketEventType type);
  void broadcastDeletion(Long channelId, Long messageId);
  void broadcastSystemEvent(Long channelId, SocketEventType type, Object data);
}
  