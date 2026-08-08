package com._labor.fakecord.infrastructure.cache.services;

import java.util.Optional;

import com._labor.fakecord.domain.dto.ServerInviteResponseDto;

public interface ServerInviteCache {
  void put(ServerInviteResponseDto dto);
  Optional<ServerInviteResponseDto> get(String code);
  void evict(String code);
}
