package com._labor.fakecord.domain.mappper.Impl;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.TopRoleDto;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.mappper.TopRoleMapper;

@Component
public class TopRoleMapperImpl implements TopRoleMapper {

  @Override
  public TopRoleDto toDto(ServerRole entity) {
    if (entity == null) return null;

    return TopRoleDto.builder()
      .id(entity.getId())
      .position(entity.getPosition())
      .name(entity.getName())
      .colorHex(entity.getColorHex())
      .hoist(entity.isHoist())
      .build();
  }
  
}
