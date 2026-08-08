package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.entity.ServerInvite;

public interface ServerInviteService {
  ServerInvite createInvite(UUID operatorId, Long serverId, ServerInvite invite);
  void acceptInvite(UUID userId, String code);
  void removeInvite(UUID operatorId, Long serverId, String code);
  List<ServerInvite> getAllServerInvites(UUID operatorId, Long serverId);
}
