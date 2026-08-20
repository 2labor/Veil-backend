package com._labor.fakecord.domain.mappper;

import com._labor.fakecord.domain.dto.ServerDto;
import com._labor.fakecord.domain.dto.ServerSidebarResponseDto;
import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.domain.request.UpdateServerRequest;
import com._labor.fakecord.infrastructure.cache.Dto.ServerCacheDto;

public interface ServerMapper {
  ServerDto toDto(Server entity);
  ServerSidebarResponseDto toSideDto(Server entity);
  ServerCacheDto toCacheDto(Server entity);

  Server toEntity(UpdateServerRequest request);
  Server toEntity(ServerCacheDto dto);
}
