package com._labor.fakecord.services.impl;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com._labor.fakecord.repository.ChannelRepository;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.ChannelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

  private final ChannelRepository repository;
  private final MessageRepository messageRepository;
  private final IdGenerator idGenerator;
  private final ChannelMemberService memberService;

  @Override
  @Transactional
  public Channel createChannel(Long serverId, UUID creatorId, String name, ChannelType type) {
    log.info("User {} is creating channel '{}' (Type: {})", creatorId, name, type);

    Long channelId = idGenerator.nextId();
    Integer position = (serverId != null) ? repository.findAllByServerIdOrderByPositionAsc(serverId).size() : null;

    Channel channel = Channel.builder()
      .id(channelId)
      .serverId(serverId)
      .name(name)
      .type(type)
      .lastActivityAt(Instant.now())
      .build();
    
    if (position != null) channel.setPosition(position);

    Channel savedChannel = repository.save(channel);
    memberService.addMember(channelId, creatorId);

    return savedChannel;
  }

  @Override
  @Transactional
  public Channel startDirectMessage(UUID creatorId, UUID recipientId) {
    return repository.findExistingDM(creatorId, recipientId)
      .orElseGet(() -> {
        Channel dm = createChannel(null, creatorId, null, ChannelType.DM);
        memberService.addMember(dm.getId(), recipientId);
        return dm;
      });
  }

  @Override
  @Transactional
  public Channel createGroupChat(UUID creatorId, List<UUID> participantIds) {
    log.info("User {} creating group chat with {} people", creatorId, participantIds.size());

    Set<UUID> uniqueUsers = new HashSet<>(participantIds);
    uniqueUsers.add(creatorId);

    Long channelId = idGenerator.nextId();
    Channel group = Channel.builder()
      .id(channelId)
      .type(ChannelType.GROUP_DM)
      .name(null)
      .lastActivityAt(Instant.now())
      .build();

    Channel saved = repository.save(group);

    uniqueUsers.forEach(userId -> memberService.addMember(saved.getId(), userId));

    return saved;
  }

  @Override
  @Transactional
  public void addUserToGroup(UUID operatorId, Long channelId, UUID targetUserId) {
    log.info("User {} is adding {} to channel {}", operatorId, targetUserId, channelId);

    Channel channel = repository.findById(channelId)
      .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

    if (!memberService.isMember(channelId, operatorId)) {
      throw new RuntimeException("Access denied: You are not in this channel");
    }    

    switch(channel.getType()) {
      case DM -> {
        log.info("Forking DM {} into a new Group DM", channelId);
        List<UUID> participants = memberService.getMemberIds(channelId);
        createGroupChat(operatorId, participants);
      }

      case GROUP_DM -> {
        if (memberService.isMember(channelId, targetUserId)) {
          log.debug("User {} already in group {}", targetUserId, channelId);
          return;
        }

        memberService.addMember(channelId, targetUserId);
        updateLastActivity(channelId);
      }

      case GUILD_TEXT, GUILD_VOICE -> 
        throw new RuntimeException("Cannot manually add members to server channels. Use invites/roles.");

      default ->
        throw new UnsupportedOperationException("Adding members to channel type " + channel.getType() + " is not supported");
    }
  }

  @Override
  @Transactional(readOnly = true)
  public List<Channel> getChannelsByServer(Long serverId) {
    return repository.findAllByServerIdOrderByPositionAsc(serverId);
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<Channel> getUserDirectMessages(UUID userId, Pageable pageable) {
    return repository.findActiveDirectMessages(userId, pageable);
  }

  @Override
  public void updateLastActivity(Long channelId) {
    repository.updateLastActivity(channelId, Instant.now());
  }

  @Override
  @Transactional
  public void renameChannel(Long channelId, String newName) {
    Channel channel = repository.findById(channelId)
      .orElseThrow(() -> new IllegalArgumentException("Channel not found"));
    channel.setName(newName);
    repository.save(channel);
  }

  @Override
  @Transactional
  public void reorderChannels(List<Long> channelIds) {
    List<Channel> channels = repository.findAllById(channelIds);

    Map<Long, Channel> channelMap = channels.stream()
      .collect(Collectors.toMap(Channel::getId, c -> c));

    for (int i = 0; i < channelIds.size(); i++) {
      Channel channel = channelMap.get(channelIds.get(i));
      if (channel != null) {
        channel.setPosition(i);
      }
    }

    repository.saveAll(channels);
  }

  @Override
  @Transactional
  public void deleteChannel(Long channelId, UUID operatorId) {
    log.warn("User {} is attempting to delete channel {}", operatorId, channelId);

    Channel channel = repository.findById(channelId)
      .orElseThrow(() -> new ResourceNotFoundException("Channel not found"));

    if (!memberService.isMember(channelId, operatorId)) {
      log.error("Access denied: User {} is not a member of channel {}", operatorId, channelId);
      throw new RuntimeException("You do not have permission to delete this channel");
    }

    messageRepository.deleteAllByChannelId(channelId);
    memberService.removeAllMembersFromChannel(channelId, operatorId);

    repository.delete(channel);

    log.info("Channel {} successfully deleted by {}", channelId, operatorId);
  }
  
}
