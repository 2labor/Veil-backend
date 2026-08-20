package com._labor.fakecord.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com._labor.fakecord.domain.entity.Server;

public interface ServerDomainService {
  Server createServer(UUID operatorId, String serverName, String iconUrl);
  List<Server> getUserServers(UUID userId);

  Server getServerById(UUID operatorId, Long serverId);
  Server updateServer(UUID operatorId, Long targetServerId, Server updatedEntity);
  void deleteServer(UUID operatorId, Long serverId);
  void updateServerPositions(UUID operatorId, Map<Long, Integer> serverPositions);

  void incrementMemberCounter(Long serverId);
  void decrementMemberCounter(Long serverId);
}
