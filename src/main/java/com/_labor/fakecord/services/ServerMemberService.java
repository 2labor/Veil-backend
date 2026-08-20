package com._labor.fakecord.services;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com._labor.fakecord.domain.dto.ServerMemberSidebarResponseDto;
import com._labor.fakecord.domain.entity.ServerMember;

public interface ServerMemberService {
  ServerMember addMemberToServer(UUID userId, Long serverId);
  void removeMemberFromServer(UUID userId, Long serverId);

  boolean checkIsUserMember(Long serverId, UUID userId);
  Slice<ServerMemberSidebarResponseDto> getServerMembers(Long serverId, UUID currentId, Pageable pageable);
  ServerMember getMemberWithRoles(UUID operatorId, UUID userId, Long serverId);
  ServerMember getServerMember(UUID userId, Long serverId);
  int getMemberMaxRolePosition(UUID operatorId, UUID userId, Long serverId);
}