package com._labor.fakecord.domain.mappper;

import com._labor.fakecord.domain.dto.TopRoleDto;
import com._labor.fakecord.domain.entity.ServerRole;

public interface TopRoleMapper {
  TopRoleDto toDto(ServerRole entity);
}