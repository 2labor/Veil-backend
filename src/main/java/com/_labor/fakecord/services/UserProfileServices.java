package com._labor.fakecord.services;

import java.util.UUID;

import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileUpdateDto;
import com._labor.fakecord.domain.entity.User;

public interface UserProfileServices {
  UserProfileFullDto getById(UUID userId);
  UserProfileFullDto update(UUID userId, UserProfileUpdateDto updateDto);
  void createDefaultProfile(User user, String displayName);
}