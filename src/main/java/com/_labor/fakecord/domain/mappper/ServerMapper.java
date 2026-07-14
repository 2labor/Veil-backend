package com._labor.fakecord.domain.mappper;

import com._labor.fakecord.domain.dto.ServerDto;
import com._labor.fakecord.domain.dto.ServerSidebarResponseDto;
import com._labor.fakecord.domain.entity.Server;

public interface ServerMapper {
  ServerDto toDto(Server entity);
  ServerSidebarResponseDto toSideDto(Server entity);
}
