package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.UUID;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.domain.entity.ChannelMemberId;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.domain.mappper.ChannelMemberMapper;
import com._labor.fakecord.repository.ChannelMemberRepository;
import com._labor.fakecord.repository.ChannelRepository;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.ChannelMemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelMemberServiceImpl implements ChannelMemberService {

  private final ChannelMemberRepository repository;
  private final ChannelRepository channelRepository;
  private final MessageRepository messageRepository;
  private final ChannelMemberMapper mapper;

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
  public List<UUID> getMemberIds(Long channelId) {
    log.debug("Fetching all member IDs for channel {}", channelId);
    return repository.findAllUserIdsByChannelId(channelId);
  }

  @Override
  @Transactional
  public void leaveMember(Long channelId, UUID userId) {
    log.info("User {} attempting leave from channel {}", userId, channelId);

    Channel channel = channelRepository.findById(channelId)
      .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

    if (channel.getType() == ChannelType.GROUP_DM && channel.getOwnerId().equals(userId)) {
      if (repository.countById_ChannelId(channelId) > 1) {
        throw new RuntimeException("You have to transfer ownership rights before leaving the group!");
      }
    }

    performRemove(channel, userId);
    log.info("User {} left channel {}", userId, channelId);
  }

  @Override
  @Transactional
  public void kickMember(Long channelId, UUID targetId, UUID operatorId) {
    Channel channel = channelRepository.findById(channelId)
      .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

    if (channel.getType() != ChannelType.GROUP_DM) {
      throw new IllegalArgumentException("You can apply kick function only to channels with dm-group type");
    }

    if (!channel.getOwnerId().equals(operatorId)) {
      throw new RuntimeException("Only owner of group can apply kick function");
    }

    if (targetId.equals(operatorId)) {
      throw new RuntimeException("You cannot kick yourself, use leave instead");
    }

    performRemove(channel, targetId);
    log.info("Operator {} kicked user {} from channel {}", operatorId, targetId, channelId);
  }

  private void performRemove(Channel channel, UUID userId) {
    repository.deleteById(new ChannelMemberId(channel.getId(), userId));

    if (channel.getType() == ChannelType.GROUP_DM) {
      if (repository.countById_ChannelId(channel.getId()) == 0) {
        channelRepository.delete(channel);
      }
    }
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

  @Override
  @Transactional
  public void removeAllMembersFromChannel(Long channelId, UUID operatorId) {
    log.info("Attempting to remove all members from channel {} by operator {}", channelId, operatorId);

    if (!channelRepository.existsById(channelId)) {
      throw new ResourceNotFoundException("Channel not found");
    }

    if (!repository.existsById_ChannelIdAndId_UserId(channelId, operatorId)) {
      throw new RuntimeException("Access denied: Operator is not a member of this channel");
    }

    repository.deleteAllByChannelId(channelId);

    log.info("All members removed from channel {}", channelId);    
  }

  @Override
  @Transactional(readOnly = true)
  public UUID getRecipientId(Long channelId, UUID myId) {
    log.debug("Finding recipient for DM channel {} excluding user {}", channelId, myId);

    return repository.findFirstRecipientId(channelId, myId)
      .orElseThrow(() -> new RuntimeException("Recipient not found or you are the only member"));
  }

  @Override
  public int getUnreadCount(Long channelId, UUID userId) {
    ChannelMember member = repository.findById_ChannelIdAndId_UserId(channelId, userId)
      .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    Long lastReadId = member.getLastReadMessageId();
    if (lastReadId == null) lastReadId = 0L;

    return (int) messageRepository.countByChannelIdAndIdGreaterThan(channelId, lastReadId);
  }

  @Override
  @Transactional
  public void addMembers(UUID operatorId, Long channelId, List<UUID> userIds) {
    log.debug("Adding {} members to channel {}", userIds.size(), channelId);

    Channel channel = channelRepository.findById(channelId)
      .orElseThrow(() -> new IllegalArgumentException("No channel with given id"));
    
    if (channel.getType() == ChannelType.GUILD_TEXT || channel.getType() == ChannelType.GUILD_VOICE) {
      throw new AccessDeniedException("Cannot manually add members to a guild channel. Manage server members instead.");
    }
      
    if (channel.getType() == ChannelType.DM) {
      throw new IllegalStateException("Cannot add members to a private DM. Create a group instead.");
    }

    if (channel.getType() == ChannelType.GROUP_DM && !isMember(channelId, operatorId)) {
      throw new AccessDeniedException("You are not a member of this group");
    }

    List<UUID> uniqueIds = userIds.stream().distinct().toList();

    List<UUID> existingId = repository.findAllUserIdsByChannelIdAndUserIdIn(channelId, uniqueIds);

    List<UUID> newUsers = uniqueIds.stream().filter(
      id -> !existingId.contains(id)
    ).toList();

    if(newUsers.isEmpty()) return;

    List<ChannelMember> entities = newUsers.stream().map(
      userId -> mapper.toEntity(channelId, userId)
    ).toList();

    repository.saveAll(entities);
  }

    @Override
    public void transferOwnership(Long channelId, UUID currentOwnerId, UUID newOwnerId) {
      log.info("Attempting to transfer ownership of channel {} from {} to {}", channelId, currentOwnerId, newOwnerId);

      Channel channel = channelRepository.findById(channelId)
        .orElseThrow(() -> new IllegalArgumentException("Channel not found!"));

      if (channel.getType() != ChannelType.GROUP_DM) {
        throw new IllegalArgumentException("Ownership can only be transferred in group chats");
      }

      if (!channel.getOwnerId().equals(currentOwnerId)) {
        throw new AccessDeniedException("You have to be owner for using this method");
      }

      if(!isMember(channelId, newOwnerId)) {
        throw new IllegalArgumentException("User not contain in this group!");
      }

      if (currentOwnerId.equals(newOwnerId)) {
        throw new IllegalArgumentException("You cannot transfer ownership to yourself");
      }

      channel.setOwnerId(newOwnerId);
      channelRepository.save(channel);

      log.info("Ownership of channel {} successfully transferred to {}", channelId, newOwnerId);
    }
}
