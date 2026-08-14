package com._labor.fakecord.domain.mappper.Impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ChannelOverrideResponseDto;
import com._labor.fakecord.domain.entity.ChannelPermissionOverride;
import com._labor.fakecord.domain.mappper.ChannelPermissionOverrideMapper;
@Component
public class ChannelPermissionOverrideMapperImpl implements ChannelPermissionOverrideMapper {

  @Override
  public ChannelOverrideResponseDto toResponseDto(ChannelPermissionOverride entity) {
    if (entity == null) return null;

    return ChannelOverrideResponseDto.builder()
      .id(entity.getId())
      .channelId(entity.getChannelId())
      .holderId(entity.getHolderId())
      .holderType(entity.getHolderType())
      .allowMask(entity.getAllowMask())
      .denyMask(entity.getDenyMask())
      .build();
  }

  @Override
  public List<ChannelOverrideResponseDto> toResponseDtos(List<ChannelPermissionOverride> entities) {
    if (entities == null || entities.isEmpty()) return List.of();

    return entities.stream()
      .map(this::toResponseDto)
      .toList();
  }
  
}
