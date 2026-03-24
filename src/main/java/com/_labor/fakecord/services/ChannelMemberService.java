package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.entity.ChannelMember;

public interface ChannelMemberService {
  Slice<ChannelMember> getMembers(Long channelId, Pageable pageable);
  List<UUID> getMemberIds(Long channelId);
  UUID getRecipientId(Long channelId, UUID myId);
  int getUnreadCount(Long channelId, UUID userId);
  boolean isMember(Long channelId, UUID userId);
  void addMember(Long channelId, UUID userId);
  void addMembers(UUID operatorId, Long channelId, List<UUID> userIds);
  void removeMember(Long channelId, UUID userId);
  void updateLastReadMessage(Long channelId, UUID userId, Long messageId);
  void removeAllMembersFromChannel(Long channelId, UUID operatorId);
}
