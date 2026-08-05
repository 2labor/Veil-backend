package com._labor.fakecord.domain.dto;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.enums.UserStatus;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder
public record ServerMemberResponseDto(
  // ServerMember properties
  @JsonSerialize(using = ToStringSerializer.class)
  UUID userId,
  @JsonSerialize(using = ToStringSerializer.class)
  Long serverId,
  String userLocalName,
  List<ServerRoleShortDto> roles,
  @JsonSerialize(using = ToStringSerializer.class)
  Long joinedAt,
  
  // UserProfile properties
  String globalName,
  String handle,
  String discriminator,
  @JsonSerialize(using = ToStringSerializer.class)
  Long globalId,
  String bio,
  String avatarUrl,
  String bannerUrl,
  UserStatus status,
  UserStatus statusPreference,
  List<ConnectionDto> connections
) {}