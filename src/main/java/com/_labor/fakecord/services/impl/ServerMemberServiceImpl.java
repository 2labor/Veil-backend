package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com._labor.fakecord.repository.ServerMemberRepository;
import com._labor.fakecord.services.ServerMemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerMemberServiceImpl implements ServerMemberService {

  private final ServerMemberRepository repository;

  @Override
  public boolean checkIsUserMember(Long serverId, UUID userId) {
    return repository.existsById_ServerIdAndId_UserId(serverId, userId);
  }
  
}
