package com._labor.fakecord.domain.mappper;

import java.util.List;

import com._labor.fakecord.domain.dto.ServerRoleCreateDto;
import com._labor.fakecord.domain.dto.ServerRoleResponseDto;
import com._labor.fakecord.domain.dto.ServerRoleShortDto;
import com._labor.fakecord.domain.dto.ServerRoleUpdateDto;
import com._labor.fakecord.domain.entity.ServerRole;

public interface ServerRoleMapper {
  ServerRole toEntity(ServerRoleCreateDto dto);
  ServerRole toEntity(ServerRoleUpdateDto dto, long roleId);
  ServerRoleResponseDto toDto(ServerRole entity);
  ServerRoleShortDto toShortDto(ServerRole entity);
  List<ServerRoleResponseDto> toDtoList(List<ServerRole> entities);
  List<ServerRoleShortDto> toShortDtoList(List<ServerRole> entities);
}