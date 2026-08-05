package com._labor.fakecord.services;

import java.util.UUID;

import com._labor.fakecord.domain.dto.ServerMemberResponseDto;

public interface ServerMemberFacade {
  ServerMemberResponseDto getMemberProfile(UUID operantId, UUID userId, Long serverId);
}