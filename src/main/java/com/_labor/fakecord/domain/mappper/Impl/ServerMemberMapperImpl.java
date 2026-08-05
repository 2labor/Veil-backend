package com._labor.fakecord.domain.mappper.Impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ServerMemberResponseDto;
import com._labor.fakecord.domain.dto.ServerMemberSidebarResponseDto;
import com._labor.fakecord.domain.dto.ServerRoleShortDto;
import com._labor.fakecord.domain.dto.TopRoleDto;
import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.ServerMemberMapper;
import com._labor.fakecord.domain.mappper.ServerRoleMapper;
import com._labor.fakecord.domain.mappper.TopRoleMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ServerMemberMapperImpl implements ServerMemberMapper {

  private final TopRoleMapper topRoleMapper;
  private final ServerRoleMapper roleMapper;

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
    TopRoleDto topRoleDto = topRole == null ? null : topRoleMapper.toDto(topRole);
    String displayName = entity.getUserLocalName() != null ? entity.getUserLocalName() : userProfile.displayName();
    return ServerMemberSidebarResponseDto.builder()
      .userId(entity.getId().getUserId())
      .displayName(displayName)
      .avatarUrl(userProfile.avatarUrl())
      .status(userProfile.statusPreference() != null ? userProfile.statusPreference() : UserStatus.OFFLINE)
      .topRole(topRoleDto)
    .build();
  }

  @Override
  public ServerMemberResponseDto toDto(ServerMember entity, UserProfileFullDto userProfileDto) {
    if (entity == null) return null;

    List<ServerRoleShortDto> rolesDto = (entity.getRoles() == null || entity.getRoles().isEmpty()) ? List.of() :
      entity.getRoles().stream()
        .sorted((r1, r2) -> Integer.compare(r2.getPosition(), r1.getPosition()))
        .map(roleMapper::toShortDto)
        .toList();

    return ServerMemberResponseDto.builder()
      // ServerMember properties
      .userId(entity.getId().getUserId())
      .serverId(entity.getId().getServerId())
      .userLocalName(entity.getUserLocalName())
      .roles(rolesDto)
      .joinedAt(entity.getJoinedAt().toEpochMilli())

      // UserProfile properties
      .globalName(userProfileDto.displayName())
      .handle(userProfileDto.handle())
      .discriminator(userProfileDto.discriminator())
      .globalId(userProfileDto.globalId())
      .bio(userProfileDto.bio())
      .avatarUrl(userProfileDto.avatarUrl())
      .bannerUrl(userProfileDto.bannerUrl())
      .status(userProfileDto.status())
      .statusPreference(userProfileDto.statusPreference())
      .connections(userProfileDto.connections())
      .build();
  }
}
