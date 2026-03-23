package com._labor.fakecord.domain.mappper.Impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ChannelDto;
import com._labor.fakecord.domain.dto.DirectMessageChannelDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.mappper.ChannelMapper;

@Component
public class ChannelMapperImpl implements ChannelMapper {

  @Override
  public ChannelDto toDto(Channel entity) {
    if (entity == null) return null;

    return new ChannelDto(
      entity.getId(),
      entity.getName(),
      entity.getType(),
      entity.getServerId(),
      entity.getLastMessageId(),
      entity.getLastActivityAt()
    );
  }

  @Override
  public Channel fromDto(ChannelDto dto) {
    if (dto == null) return null;

    return Channel.builder()  
      .id(dto.id())
      .serverId(dto.serverId())
      .name(dto.name())
      .type(dto.type())
      .lastActivityAt(dto.lastActivity())
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
      entity.getLastActivityAt(),
      unreadCount
    );
  }

  @Override
  public List<ChannelDto> toDtoList(List<Channel> channels) {
    if (channels == null || channels.isEmpty()) return Collections.emptyList();

    return channels.stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }
  
}
