package com._labor.fakecord.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.infrastructure.cache.Dto.ServerCacheDto;

public interface ServerDomainService {
  Server createServer(UUID operatorId, String serverName, String iconUrl);
  List<Server> getUserServers(UUID userId);

  ServerCacheDto getMetadata(Long serverId);

  Server getServerById(UUID operatorId, Long serverId);
  Server updateServer(UUID operatorId, Long targetServerId, Server updatedEntity);
  void deleteServer(UUID operatorId, Long serverId);
  void updateServerPositions(UUID operatorId, Map<Long, Integer> serverPositions);

  Server transferOwnership(UUID operatorId, Long serverId, UUID targetMemberId);

  void incrementMemberCounter(Long serverId);
  void decrementMemberCounter(Long serverId);
}
