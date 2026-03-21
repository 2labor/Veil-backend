package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.domain.entity.ChannelMemberId;
import com._labor.fakecord.repository.ChannelMemberRepository;
import com._labor.fakecord.repository.ChannelRepository;
import com._labor.fakecord.services.ChannelMemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelMemberServiceImpl implements ChannelMemberService {

  private final ChannelMemberRepository repository;
  private final ChannelRepository channelRepository;

  @Override
  @Transactional
  public void addMember(Long channelId, UUID userId) {
    log.info("Adding user {} to channel {}", userId, channelId);

    if(!channelRepository.existsById(channelId)) {
      throw new RuntimeException("Channel not found");
    }

    ChannelMemberId memberId = new ChannelMemberId(channelId, userId);

    if (repository.existsById(memberId)) {
      log.debug("User {} is already a member of channel {}", userId, channelId);
      return;
    }

    ChannelMember member = ChannelMember.builder()
      .id(memberId)
      .build();

    repository.save(member);
  }

  @Override
  @Transactional
  public void removeMember(Long channelId, UUID userId) {
    log.info("Removing user {} from channel {}", userId, channelId);
    ChannelMemberId memberId = new ChannelMemberId(channelId, userId);

    if (!repository.existsById(memberId)) {
      throw new RuntimeException("Member not found in this channel");
    }

    repository.deleteById(memberId);
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<ChannelMember> getMembers(Long channelId, Pageable pageable) {
    log.debug("Fetching members slice for channel {}", channelId);
    return repository.findAllById_ChannelId(channelId, pageable);
  }

  @Override
  @Transactional
  public void updateLastReadMessage(Long channelId, UUID userId, Long messageId) {
    log.debug("Updating last read message for user {} in channel {} to {}", userId, channelId, messageId);

    ChannelMember member = repository.findById_ChannelIdAndId_UserId(channelId, userId)
      .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    member.setLastReadMessageId(messageId);
    repository.save(member);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isMember(Long channelId, UUID userId) {
    return repository.existsById_ChannelIdAndId_UserId(channelId, userId);
  }
  
}
