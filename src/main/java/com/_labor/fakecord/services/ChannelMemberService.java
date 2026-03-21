package com._labor.fakecord.services;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.entity.ChannelMember;

public interface ChannelMemberService {
  void addMember(Long channelId, UUID userId);
  void removeMember(Long channelId, UUID userId);
  Slice<ChannelMember> getMembers(Long channelId, Pageable pageable);
  void updateLastReadMessage(Long channelId, UUID userId, Long messageId);
  boolean isMember(Long channelId, UUID userId);
}
