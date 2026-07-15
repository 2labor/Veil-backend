package com._labor.fakecord.services;

import java.util.UUID;

public interface ServerMemberService {
  boolean checkIsUserMember(Long serverId, UUID userId);
}
