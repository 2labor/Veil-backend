package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.ServerMemberSidebarResponseDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.mappper.ServerMemberMapper;
import com._labor.fakecord.repository.ServerMemberRepository;
import com._labor.fakecord.services.ServerMemberService;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerMemberServiceImpl implements ServerMemberService {

  private final ServerMemberRepository repository;
  private final ServerMemberMapper mapper;
  private final UserProfileCache userProfileService;

  @Override
  public boolean checkIsUserMember(Long serverId, UUID userId) {
    return repository.existsById_ServerIdAndId_UserId(serverId, userId);
  }

  @Override
  public Slice<ServerMemberSidebarResponseDto> getServerMembers(Long serverId, UUID currentId, Pageable pageable) {
    if (!checkIsUserMember(serverId, currentId)){
      throw new AccessDeniedException("You do not have access to this server!");
    }

    Slice<ServerMember> members = repository.findAllByServerIdWithRoles(serverId, pageable);

    List<UUID> userIds = members.stream().map(m -> m.getId().getUserId()).toList();

    Map<UUID, UserProfileShort> userProfiles = userProfileService.getUserProfileShortBatch(userIds);

    List<ServerMemberSidebarResponseDto> dtos = mapper.toSidebarDtos(
      members.getContent(), 
      userProfiles
    );

    return new SliceImpl<ServerMemberSidebarResponseDto>(dtos, pageable, members.hasNext());
  }
  
}
