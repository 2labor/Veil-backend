package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com._labor.fakecord.repository.ServerRepository;
import com._labor.fakecord.services.ServerSecurityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerSecurityServiceImpl implements ServerSecurityService {
  
  private final ServerRepository repo;

  @Override
  public boolean isUserOwner(UUID userId, Long serverId) {
    if (userId == null) return false;
    return repo.existsByIdAndOwnerId(serverId, userId);
  }

}
