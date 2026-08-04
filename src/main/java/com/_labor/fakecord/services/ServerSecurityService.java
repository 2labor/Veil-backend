package com._labor.fakecord.services;

import java.util.UUID;

public interface ServerSecurityService {
  boolean isUserOwner(UUID userId, Long serverId);
}
