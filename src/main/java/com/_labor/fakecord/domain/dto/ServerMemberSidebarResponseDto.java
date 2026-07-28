package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com._labor.fakecord.domain.enums.UserStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record ServerMemberSidebarResponseDto(
  @JsonSerialize(using = ToStringSerializer.class)
  UUID userId,
  
  String displayName,
  String avatarUrl,
  UserStatus status,
  
  TopRoleDto topRole
) {}