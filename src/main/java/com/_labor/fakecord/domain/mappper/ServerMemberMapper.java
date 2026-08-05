package com._labor.fakecord.domain.mappper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com._labor.fakecord.domain.dto.ServerMemberResponseDto;
import com._labor.fakecord.domain.dto.ServerMemberSidebarResponseDto;
import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ServerMember;

public interface ServerMemberMapper {
  List<ServerMemberSidebarResponseDto> toSidebarDtos(List<ServerMember> members, Map<UUID, UserProfileShort> userProfiles);
  ServerMemberResponseDto toDto(ServerMember entity, UserProfileFullDto userProfileDto);
}
