package com._labor.fakecord.services;

import com._labor.fakecord.domain.entity.Message;

public interface MessageBroadcaster {
  void broadcast(Message message);
  void broadcastDeletion(Long channelId, Long messageId);
}
