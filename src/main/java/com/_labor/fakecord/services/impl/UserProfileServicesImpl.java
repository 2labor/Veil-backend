package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileUpdateDto;
import com._labor.fakecord.domain.entity.User;
import com._labor.fakecord.domain.entity.UserProfile;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.exception.ProfileNotFoundException;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.UserProfileRepository;
import com._labor.fakecord.services.HandleGeneratorService;
import com._labor.fakecord.services.UserProfileCache;
import com._labor.fakecord.services.UserProfileServices;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserProfileServicesImpl implements UserProfileServices{
  
  private final UserProfileMapper mapper;
  private final UserProfileRepository repository;
  private final UserProfileCache cache;
  private final OutboxService outboxService;
  private final HandleGeneratorService generation;

  public UserProfileServicesImpl(UserProfileMapper mapper, UserProfileRepository repository, UserProfileCache cache, OutboxService outboxService, HandleGeneratorService generation) {
    this.mapper = mapper;
    this.repository = repository;
    this.cache = cache;
    this.outboxService = outboxService;
    this.generation = generation;
  }
  
  @Override
  @Transactional(readOnly = true)
  public UserProfileFullDto getById(UUID userId) {
    return cache.getUserProfile(userId);
  }

  @Override
  @Transactional
  public UserProfileFullDto update(UUID userId, UserProfileUpdateDto updateDto) {
    UserProfile profile = repository.findById(userId)
      .orElseThrow(() -> new ProfileNotFoundException(userId));
    
    if (updateDto.handle() != null && !updateDto.handle().equals(profile.getHandle())) {
      if (profile.isHandleSet()) {
          throw new IllegalStateException("Handle can only be changed once!");
      }

      String newHandle = generation.prepareHandle(updateDto.handle());
        
      String newDiscriminator = generation.generateUniqueDiscriminator(newHandle);
      
      profile.setHandle(newHandle);
      profile.setDiscriminator(newDiscriminator);
      profile.setHandleSet(true);
      
      log.info("User {} changed handle to {}#{}", userId, newHandle, newDiscriminator);
    }

    mapper.toUpdateDto(updateDto, profile);
    UserProfile savedProfile = repository.save(profile);

    cache.evict(userId);

    outboxService.publish(
      userId, 
      OutboxEventType.USER_PROFILE_UPDATED, 
      "{}"
    );

    log.info("Profile updated and outbox event published for user: {}", userId);  

    return cache.getUserProfile(userId);
  }

  @Override
  public void createDefaultProfile(User user, String displayName) {

    String baseName = (user.getAccount() != null && user.getAccount().getLogin() != null) 
    ? user.getAccount().getLogin() 
    : displayName;

    String finalHandle = generation.prepareHandle(baseName);
    String discriminator = generation.generateUniqueDiscriminator(finalHandle);

    UserProfile profile = new UserProfile();
    profile.setUser(user);
    profile.setDisplayName(displayName);
    profile.setHandle(finalHandle);
    profile.setDiscriminator(discriminator);
    profile.setHandleSet(false);
    repository.save(profile);
  }
  
}
