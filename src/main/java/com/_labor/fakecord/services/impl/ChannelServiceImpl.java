package com._labor.fakecord.services.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.ChannelResponseDto;
import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.domain.enums.NotificationPriority;
import com._labor.fakecord.domain.enums.NotificationType;
import com._labor.fakecord.domain.enums.SocketEventType;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.ChannelMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.domain.notifications.SystemNotification;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com._labor.fakecord.infrastructure.outbox.domain.payload.ChannelCreatedPayload;
import com._labor.fakecord.repository.ChannelRepository;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.ChannelService;
import com._labor.fakecord.services.MessageBroadcaster;
import com._labor.fakecord.services.NotificationService;
import com._labor.fakecord.services.ServerMemberService;
import com._labor.fakecord.services.UserProfileCache;

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
  private final NotificationService notificationService;
  private final ChannelMapper mapper;
  private final UserProfileCache profileCache;
  private final UserProfileMapper profileMapper;
  private final ServerMemberService serverMemberService;

  @Override
  @Transactional
  public Channel createChannel(Long serverId, UUID creatorId, String name, ChannelType type, Long parentId) {
    log.info("User {} is creating channel '{}' (Type: {})", creatorId, name, type);

    validateServerAssociation(serverId, type);

    Channel parentCategory = null;
    if (parentId != null) {
      if (type == ChannelType.GUILD_CATEGORY) {
        throw new IllegalArgumentException("A category cannot be placed inside another category");
      }

      parentCategory = repository.findById(parentId)
        .orElseThrow(() -> new IllegalArgumentException("No category with id:" + parentId));

      validateParentCategory(parentCategory, serverId);
    }

    Long channelId = idGenerator.nextId();
    Integer position = (serverId != null) ? (int) repository.countByServerId(serverId) : null;

    Channel channel = switch (type) {
      case GUILD_CATEGORY -> Channel.builder()
        .id(channelId)
        .serverId(serverId)
        .name(name)
        .type(type)
        .build();
      
      case GROUP_DM, GUILD_TEXT, DM, GUILD_VOICE -> Channel.builder()
        .id(channelId)
        .serverId(serverId)
        .name(name)
        .type(type)
        .parent(parentCategory)
        .lastActivityAt(Instant.now())
        .build();

      default -> throw new IllegalArgumentException("Unsupported channel type: " + type);
    };

    if (position != null) channel.setPosition(position);
    Channel savedChannel = repository.save(channel);

    if (type != ChannelType.GUILD_CATEGORY) {
      memberService.addMember(channelId, creatorId);
    }

    return savedChannel;
  }

  @Override
  @Transactional
  public Channel startDirectMessage(UUID creatorId, UUID recipientId) {
    return repository.findExistingDM(creatorId, recipientId)
      .orElseGet(() -> {
        Channel dm = createChannel(null, creatorId, null, ChannelType.DM, null);
        memberService.addMember(dm.getId(), recipientId);

        SystemNotification<ChannelCreatedPayload> notification = SystemNotification.of(
          dm.getId(),
          null,
          NotificationType.DM_CREATE,
          NotificationPriority.HIGH,
          new ChannelCreatedPayload(dm.getId(), creatorId, ChannelType.DM)
        );

        notificationService.sendToUser(recipientId, notification);
        return dm;
      });
  }

  @Override
  @Transactional
  public Channel createGroupChat(UUID creatorId, List<UUID> participantIds, String name) {
    log.info("User {} creating group chat with {} people", creatorId, participantIds.size());

    Set<UUID> uniqueUsers = new HashSet<>(participantIds);
    uniqueUsers.add(creatorId);

    Long channelId = idGenerator.nextId();
    Channel group = Channel.builder()
      .id(channelId)
      .type(ChannelType.GROUP_DM)
      .name(name)
      .ownerId(creatorId)
      .lastActivityAt(Instant.now())
      .build();

    Channel saved = repository.save(group);

    // TODO: Refactor to achive less db queries
    memberService.addMember(saved.getId(), creatorId);

    memberService.addMembers(creatorId, saved.getId(), uniqueUsers.stream().toList());

    SystemNotification<ChannelCreatedPayload> notification = SystemNotification.of(
      saved.getId(),
      null,
      NotificationType.GROUP_CREATE,
      NotificationPriority.HIGH,
      new ChannelCreatedPayload(saved.getId(), creatorId, ChannelType.GROUP_DM)
    );

    uniqueUsers.stream()
      .filter(userId -> !userId.equals(creatorId))
      .forEach(userId -> notificationService.sendToUser(userId, notification));

    return saved;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Channel> getChannelsByServer(Long serverId, UUID userId) {
    List<Channel> channels = repository.findAllByServerIdAndUserId(serverId, userId);

    if (channels.isEmpty()) {
      boolean isUserMember = serverMemberService.checkIsUserMember(serverId, userId);
      if (!isUserMember) {
        throw new AccessDeniedException("You do not have permission to view this server's channels.");
      }
    }

    return channels;
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
  public void reorderChannels(Long serverId, List<Long> channelIds) {
    List<Channel> channels = repository.findAllByServerIdOrderByPositionAsc(serverId);

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

  @Override
  @Transactional(readOnly = true)
  public List<Channel> getServerChannels(Long serverId) {
    return repository.findAllByServerIdOrderByPositionAsc(serverId);
  }

  private void validateServerAssociation(Long serverId, ChannelType type) {
    if (type.isGuildType()) {
      if (serverId == null) {
        throw new IllegalArgumentException("Server ID cannot be null for guild channels (type: " + type + ")");
      }
    } else {
      if (serverId != null) {
        throw new IllegalArgumentException("Server ID must be null for non-guild channels (type: " + type + ")");
      }
    }
  }

  private void validateParentCategory(Channel parent, Long currentServerId) {
    if (parent.getType() != ChannelType.GUILD_CATEGORY) {
      throw new IllegalArgumentException("Parent channel cannot be different form " + ChannelType.GUILD_CATEGORY);
    }

    if (!parent.getServerId().equals(currentServerId)) {
      throw new IllegalArgumentException("Parent channel cannot be on different server with children channel!");
    }
  }
}
