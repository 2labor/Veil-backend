package com._labor.fakecord.services.impl;

import com._labor.fakecord.domain.dto.ServerInviteResponseDto;
import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.ServerInvite;
import com._labor.fakecord.domain.mappper.ServerInviteMapper;
import com._labor.fakecord.infrastructure.cache.services.ServerInviteCache;
import com._labor.fakecord.repository.ServerInviteRepository;
import com._labor.fakecord.repository.ServerRepository;
import com._labor.fakecord.security.invites.InviteCodeGenerator;
import com._labor.fakecord.services.ServerInviteService;
import com._labor.fakecord.services.ServerMemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerInviteServiceImpl implements ServerInviteService {

  private final ServerInviteCache cache;
  private final ServerInviteRepository repository;
  private final InviteCodeGenerator codeGenerator;
  private final ServerInviteMapper mapper;
  private final ServerMemberService serverMemberService;
  private final ServerRepository serverRepository;

  @Transactional
  @Override
  public ServerInvite createInvite(UUID operatorId, Long serverId, ServerInvite invite) {
    String code = codeGenerator.generateCode();
    ServerInvite inviteEntity = ServerInvite.builder()
      .code(code)
      .serverId(serverId)
      .creatorId(operatorId)
      .maxUsed(invite.getMaxUsed())
      .countUsed(invite.getCountUsed() != null ? invite.getCountUsed() : 0)
      .expiredAt(invite.getExpiredAt())
      .build();

    ServerInvite saved = repository.save(inviteEntity);
    ServerInviteResponseDto dto = mapper.toDto(saved);
    cache.put(dto);
    return saved;
  }

  @Transactional
  @Override
  public void acceptInvite(UUID userId, String code) {
    ServerInviteResponseDto serverInvite = cache.get(code)
      .orElseThrow(() -> new IllegalArgumentException("No code with such code!"));

    if (serverMemberService.checkIsUserMember(serverInvite.serverId(), userId)) {
      throw new IllegalArgumentException("You already a member of a server!");
    }

    int updatedRows = repository.incrementUsesCount(code);
    if (updatedRows == 0) {
      cache.evict(code);
      throw new IllegalArgumentException("Invite code has reached its usage limit or expired");
    }

    cache.evict(code);
    serverRepository.incrementMemberCount(serverInvite.serverId());

    serverMemberService.addMemberToServer(userId, serverInvite.serverId());
  }

  @Transactional
  @Override
  public void removeInvite(UUID operatorId, Long serverId, String code) {
    ServerInvite invite = repository.findById(code)
    .orElseThrow(() -> new IllegalArgumentException("No invitation with such id!"));
    
    if (!invite.getServerId().equals(serverId)) {
      throw new AccessDeniedException("Invite does not belong to this server");
    }


    repository.delete(invite);
    cache.evict(code);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ServerInvite> getAllServerInvites(UUID operatorId, Long serverId) {

    return repository.findByServerId(serverId);
  }
  
}
