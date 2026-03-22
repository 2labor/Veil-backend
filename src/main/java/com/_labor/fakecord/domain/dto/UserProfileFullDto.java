package com._labor.fakecord.domain.dto;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.enums.UserStatus;

import lombok.Builder;


@Builder(toBuilder = true)
public record UserProfileFullDto(
  UUID userId,
  String displayName,
  String handle,
  String discriminator,
  Long globalId,
  String bio,
  String avatarUrl,
  String bannerUrl,
  String uploadUrl,
  Object metadata,
  UserStatus status,         
  UserStatus statusPreference,
  List<ConnectionDto> connections,
  boolean isGhost
) {
  public static class UserProfileFullDtoBuilder {
    private boolean isGhost = false; 
  }
}
