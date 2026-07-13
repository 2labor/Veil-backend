package com._labor.fakecord.services;

import com._labor.fakecord.domain.entity.ServerRole;

public interface ServerRoleService {
  ServerRole createDefaultName(Long serverId);
}
