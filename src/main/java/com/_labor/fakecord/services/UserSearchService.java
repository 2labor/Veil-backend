package com._labor.fakecord.services;

import com._labor.fakecord.domain.dto.UserProfileShort;

public interface UserSearchService {
  UserProfileShort searchByFullTag(String query);
}
