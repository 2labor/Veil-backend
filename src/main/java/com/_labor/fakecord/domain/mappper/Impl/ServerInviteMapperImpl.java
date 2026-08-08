package com._labor.fakecord.domain.mappper.Impl;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ServerInviteCreateDto;
import com._labor.fakecord.domain.dto.ServerInviteResponseDto;
import com._labor.fakecord.domain.entity.ServerInvite;
import com._labor.fakecord.domain.mappper.ServerInviteMapper;

@Component
public class ServerInviteMapperImpl implements ServerInviteMapper{

  @Override
  public ServerInviteResponseDto toDto(ServerInvite entity) {
    Long expiredAtMillis = entity.getExpiredAt() != null 
      ? entity.getExpiredAt().toEpochMilli() 
      : null;
      
    Long createdAtMillis = entity.getCreatedAt() != null 
      ? entity.getCreatedAt().toEpochMilli() 
      : null;

    return ServerInviteResponseDto.builder()  
      .code(entity.getCode())
      .serverId(entity.getServerId())
      .creatorId(entity.getCreatorId())
      .countUsed(entity.getCountUsed() != null ? entity.getCountUsed() : 0)
      .maxUsed(entity.getMaxUsed())
      .expiredAt(expiredAtMillis)
      .createdAt(createdAtMillis)
      .build();
  }

  @Override
  public ServerInvite toEntity(ServerInviteCreateDto dto) {
    if (dto == null) return new ServerInvite();

    Instant expiredAt = (dto.durationSeconds() != null && dto.durationSeconds() > 0) ? Instant.now().plusSeconds(dto.durationSeconds()) : null;

    return ServerInvite.builder()
      .maxUsed(dto.maxUsed())
      .expiredAt(expiredAt)
      .countUsed(0)
      .build();
  }

  @Override
  public List<ServerInviteResponseDto> toListDto(List<ServerInvite> serverInvites) {
    if (serverInvites == null || serverInvites.isEmpty()) return List.of();

    return serverInvites.stream().map(this::toDto).toList();
  }
}
