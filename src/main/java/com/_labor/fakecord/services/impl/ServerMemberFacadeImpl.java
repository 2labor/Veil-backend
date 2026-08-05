package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.ServerMemberResponseDto;
import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.mappper.ServerMemberMapper;
import com._labor.fakecord.services.ServerMemberFacade;
import com._labor.fakecord.services.ServerMemberService;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerMemberFacadeImpl implements ServerMemberFacade {

  private final ServerMemberMapper mapper;
  private final UserProfileCache profileService;
  private final ServerMemberService service;

  @Override
  public ServerMemberResponseDto getMemberProfile(UUID operantId, UUID userId, Long serverId) {
    ServerMember serverMember = service.getMemberWithRoles(operantId, userId, serverId);
    UserProfileFullDto profileFullDto = profileService.getUserProfile(userId);

    return mapper.toDto(serverMember, profileFullDto);
  }
  
}
