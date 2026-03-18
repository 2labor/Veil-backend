package com._labor.fakecord.domain.mappper.Impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ConnectionDto;
import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.dto.UserProfileUpdateDto;
import com._labor.fakecord.domain.entity.UserConnection;
import com._labor.fakecord.domain.entity.UserProfile;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.ConnectionMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com.rabbitmq.client.Connection;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserProfileMapperImpl implements UserProfileMapper {

  private final ConnectionMapper connectionMapper;

  @Override
  public UserProfileFullDto toFullDto(UserProfile profile, UserStatus status, List<UserConnection> connections) {
    if (null == profile) return null;

    List<ConnectionDto> connectionDtos = (null != connections) 
      ? connections.stream()
        .filter(UserConnection::isShowOnProfile)
        .map(connectionMapper::toDto)
        .toList()
      : List.of();

    return UserProfileFullDto.builder()
      .userId(profile.getId())
      .displayName(profile.getDisplayName())
      .handle(profile.getHandle())
      .discriminator(profile.getDiscriminator())
      .globalId(profile.getGlobalId())
      .bio(profile.getBio())
      .avatarUrl(profile.getAvatarUrl())
      .bannerUrl(profile.getBannerUrl())
      .uploadUrl(null) 
      .metadata(profile.getMetadata())
      .status(status != null ? status : UserStatus.OFFLINE)
      .statusPreference(profile.getStatusPreference())
      .connections(connectionDtos)
      .isGhost(false)
      .build();
  }

  @Override
  public UserProfileShort toShortDto(UserProfile profile, UserStatus status) {
    if (null == profile) return null;

    return UserProfileShort.builder()
      .userId(profile.getId())
      .displayName(profile.getDisplayName())
      .handle(profile.getHandle())
      .discriminator(profile.getDiscriminator())
      .globalId(profile.getGlobalId())
      .avatarUrl(profile.getAvatarUrl())
      .status(status != null ? status : UserStatus.OFFLINE)
      .build();
  }

  @Override
  public void toUpdateDto(UserProfileUpdateDto dto, UserProfile entity) {
    if (null == dto) return;
    if (null != dto.displayName()) {
      entity.setDisplayName(dto.displayName());
    }
    if (null != dto.bio()) {
      entity.setBio(dto.bio());
    }
    if (null != dto.statusPreference()) {
      entity.setStatusPreference(dto.statusPreference());
    }
  }

  @Override
  public UserProfileShort toShortDto(UserProfileFullDto full, UserStatus status) {
    if (null == full) return null;

    return UserProfileShort.builder()
      .userId(full.userId())
      .displayName(full.displayName())
      .handle(full.handle())
      .discriminator(full.discriminator()) 
      .globalId(full.globalId())
      .avatarUrl(full.avatarUrl())
      .status(status != null ? status : full.status())
      .build();
  }
}