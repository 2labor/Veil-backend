package com._labor.fakecord.domain.mappper.Impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ChannelDto;
import com._labor.fakecord.domain.dto.DirectMessageChannelDto;
import com._labor.fakecord.domain.dto.GroupChannelDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.mappper.ChannelMapper;

@Component
public class ChannelMapperImpl implements ChannelMapper {

  @Override
  public ChannelDto toDmDto(Channel entity) {
    if (entity == null) return null;

    return new ChannelDto(
      entity.getId(),
      entity.getName(),
      entity.getType(),
      entity.getServerId(),
      entity.getLastMessageId(),
      entity.getLastActivityAt().toEpochMilli()
    );
  }

  @Override
  public Channel fromDmDto(ChannelDto dto) {
    if (dto == null) return null;

    return Channel.builder()  
      .id(dto.id())
      .serverId(dto.serverId())
      .name(dto.name())
      .type(dto.type())
      .lastActivityAt(dto.lastActivity() == null ? Instant.ofEpochMilli(dto.lastActivity()) : null)
      .lastMessageId(dto.lastMessageId())
      .build();
  }

  @Override
  public DirectMessageChannelDto toDirectDto(Channel entity, UserProfileShort recipient, int unreadCount) {
    if (entity == null) return null;

    return new DirectMessageChannelDto(
      entity.getId(),
      recipient,
      entity.getLastMessageContent(),
      entity.getLastActivityAt().toEpochMilli(),
      unreadCount
    );
  }

  @Override
  public List<ChannelDto> toDtoList(List<Channel> channels) {
    if (channels == null || channels.isEmpty()) return Collections.emptyList();

    return channels.stream()
      .map(this::toDmDto)
      .collect(Collectors.toList());
  }

    @Override
    public GroupChannelDto toGroupDto(Channel entity, int unreadCount) {
      if (entity == null) return null;

      return GroupChannelDto.builder()
        .id(entity.getId())
        .name(entity.getName())
        .ownerId(entity.getOwnerId())
        .lastMessageContent(entity.getLastMessageContent())
        .lastActivity(entity.getLastActivityAt().toEpochMilli())
        .unreadCount(unreadCount)
        .build();
    }
  
}
