package com._labor.fakecord.services.impl;

import org.springframework.transaction.annotation.Transactional;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.repository.UserProfileRepository;
import com._labor.fakecord.services.UserSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserSearchServiceImpl implements UserSearchService {

  private final UserProfileRepository repository;
  private final UserProfileMapper mapper;
  
  @Override
  @Transactional(readOnly = true)
  public UserProfileShort searchByFullTag(String query) {
    String[] parts = parseTag(query);
    String handle = parts[0].toLowerCase();
    String discriminator = parts[1];

    return repository.findByFullHandle(handle, discriminator)
      .map(profile -> mapper.toShortDto(profile, profile.getStatusPreference()))
      .orElseThrow(() -> new ResourceNotFoundException("User not found: " + query));
  }

  private String[] parseTag(String query) {
    if (!query.contains("#")) {
      throw new IllegalArgumentException("Tag must contain #");
    }
    String[] parts = query.split("#");
    if (parts.length != 2 || parts[1].length() != 4) {
      throw new IllegalArgumentException("Invalid format. Use handle#0000");
    }
    return parts;
  }
  
}
