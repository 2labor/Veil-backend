package com._labor.fakecord.domain.mappper.Impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ServerRoleCreateDto;
import com._labor.fakecord.domain.dto.ServerRoleResponseDto;
import com._labor.fakecord.domain.dto.ServerRoleShortDto;
import com._labor.fakecord.domain.dto.ServerRoleUpdateDto;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.mappper.ServerRoleMapper;

@Component
public class ServerRoleMapperImpl implements ServerRoleMapper {

  @Override
  public ServerRole toEntity(ServerRoleCreateDto dto) {
    if (dto == null) return null;
    
    return ServerRole.builder()
      .name(dto.name())
      .hoist(dto.hoist())
      .colorHex(dto.colorHex())
      .permissions(dto.permissions())
      .build();
  }

  @Override
  public ServerRole toEntity(ServerRoleUpdateDto dto, long roleId) {
    if (dto == null) return null;

    return ServerRole.builder()
      .id(roleId)
      .name(dto.name())
      .hoist(dto.hoist())
      .colorHex(dto.colorHex())
      .permissions(dto.permissions())
      .build();
  }

  @Override
  public ServerRoleResponseDto toDto(ServerRole entity) {
    if (entity == null) return null; 

    return ServerRoleResponseDto.builder()
      .id(entity.getId())
      .name(entity.getName())
      .hoist(entity.isHoist())
      .colorHex(entity.getColorHex())
      .permissions(entity.getPermissions())
      .build();
  }

  @Override
  public ServerRoleShortDto toShortDto(ServerRole entity) {
    if (entity == null) return null;

    return ServerRoleShortDto.builder()
      .id(entity.getId())
      .name(entity.getName())
      .colorHex(entity.getColorHex())
      .position(entity.getPosition())
      .build();
  }

  @Override
  public List<ServerRoleResponseDto> toDtoList(List<ServerRole> entities) {
    if (entities == null || entities.isEmpty()) return List.of();

    return entities.stream().map(this::toDto).toList();
  }

  @Override
  public List<ServerRoleShortDto> toShortDtoList(List<ServerRole> entities) {
    if (entities == null || entities.isEmpty()) return List.of();

    return entities.stream().map(this::toShortDto).toList();
  }
  
}
