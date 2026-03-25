package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.enums.ChannelType;

public interface ChannelService {
  Channel createChannel(Long serverId, UUID creatorId, String name, ChannelType type);
  Channel startDirectMessage(UUID creatorId, UUID recipientId);
  Channel createGroupChat(UUID creatorId, List<UUID> participantIds, String name);
  List<Channel> getChannelsByServer(Long serverId);
  Slice<Channel> getUserDirectMessages(UUID userId, Pageable pageable);
  void updateLastActivity(Long channelId);
  void renameChannel(Long channelId, String newName);
  void reorderChannels(Long serverId, List<Long> channelIds);
  void deleteChannel(Long channelId, UUID operatorId);
} 