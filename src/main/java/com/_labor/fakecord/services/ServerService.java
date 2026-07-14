package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.entity.Server;

public interface ServerService {
  Server createServer(UUID operatorId, String serverName, String iconUrl);
  List<Server> getUserServers(UUID userId);
}
