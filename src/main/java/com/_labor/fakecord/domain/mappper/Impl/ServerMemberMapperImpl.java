package com._labor.fakecord.domain.mappper.Impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ServerMemberSidebarResponseDto;
import com._labor.fakecord.domain.dto.TopRoleDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.ServerMemberMapper;
import com._labor.fakecord.domain.mappper.TopRoleMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerMemberMapperImpl implements ServerMemberMapper {

  private final TopRoleMapper roleMapper;

  @Override
  public List<ServerMemberSidebarResponseDto> toSidebarDtos(
    List<ServerMember> members,
    Map<UUID, UserProfileShort> userProfiles
  ) {
    if (members == null || members.isEmpty()) return List.of();

    return members.stream().map(
      member -> {
        UUID userId = member.getId().getUserId();
        UserProfileShort profile = userProfiles.get(userId);
        return toDto(member, profile);
      }
    ).toList();
  }
  
  private ServerMemberSidebarResponseDto toDto(
    ServerMember entity,
    UserProfileShort userProfile
  ) {
    if (entity == null) return null;
    
    ServerRole topRole = entity.getTopRole();
    TopRoleDto topRoleDto = topRole == null ? null : roleMapper.toDto(topRole);
    return ServerMemberSidebarResponseDto.builder()
      .userId(entity.getId().getUserId())
      .displayName(entity.getUserLocalName())
      .avatarUrl(userProfile.avatarUrl())
      .status(userProfile.status() != null ? userProfile.status() : UserStatus.OFFLINE)
      .topRole(topRoleDto)
    .build();
  }
}
