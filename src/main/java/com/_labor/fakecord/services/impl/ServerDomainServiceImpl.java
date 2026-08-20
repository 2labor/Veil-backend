package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.domain.mappper.ServerMapper;
import com._labor.fakecord.infrastructure.cache.Dto.ServerCacheDto;
import com._labor.fakecord.infrastructure.cache.services.PermissionCache;
import com._labor.fakecord.infrastructure.cache.services.ServerCache;
import com._labor.fakecord.infrastructure.id.IdGenerator; 
import com._labor.fakecord.repository.ServerMemberRepository;
import com._labor.fakecord.repository.ServerRepository;
import com._labor.fakecord.services.ChannelService;
import com._labor.fakecord.services.ServerDomainService;
import com._labor.fakecord.services.ServerMemberService;
import com._labor.fakecord.services.ServerRoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerDomainServiceImpl implements ServerDomainService {
  private final ServerRepository repo;
  private final ServerMemberRepository memberRepository;
  private final IdGenerator idGenerator;
  private final ChannelService channelService;
  private final ServerRoleService rolesService;
  private final ServerMemberService memberService;
  private final PermissionCache permissionCache;
  private final ServerCache cacheProvider;
  private final ServerMapper mapper;

  @Override
  @Transactional
  public Server createServer(UUID operatorId, String serverName, String iconUrl) {
    Long serverId = idGenerator.nextId();

    Server server = Server.builder()
      .id(serverId)
      .ownerId(operatorId)
      .name(serverName)
      .iconUrl(iconUrl)
      .memberCounter(1)
      .build();
    Server savedServer = repo.save(server);
    
    ServerRole defaultRole = rolesService.createDefaultRole(server.getId());

    ServerMemberId memberId = new ServerMemberId(operatorId, savedServer.getId());
    ServerMember owner = ServerMember.builder()
      .id(memberId)
      .build();
    owner.addRole(defaultRole);
    memberRepository.save(owner);

    Channel parentCategory = channelService.createChannel(savedServer.getId(), operatorId, "general-category", ChannelType.GUILD_CATEGORY, null);

    channelService.createChannel(savedServer.getId(), operatorId, "general", ChannelType.GUILD_TEXT, parentCategory.getId());

    return savedServer;
  }

  @Override
  public List<Server> getUserServers(UUID userId) {
    return repo.findAllByUserIdOrderedByPosition(userId);
  }

  @Override
  @Transactional(readOnly = true)
  public Server getServerById(UUID operatorId, Long serverId) {
    if (!memberService.checkIsUserMember(serverId, operatorId)) {
      throw new AccessDeniedException("You cannot get server that you not member of!");
    } 

    ServerCacheDto cacheDto = cacheProvider.get(serverId, () -> fetchFromDb(serverId));

    return mapper.toEntity(cacheDto);
  }

  @Override
  @Transactional
  public Server updateServer(UUID operatorId, Long targetServerId, Server updatedEntity) {
    Server server = repo.findById(targetServerId)
      .orElseThrow(() -> new IllegalArgumentException("No server with such id: " + targetServerId));
  
    if (updatedEntity.getName() != null && !updatedEntity.getName().isBlank()) {
      server.setName(updatedEntity.getName());
    }

    if (updatedEntity.getDescription() != null) {
      server.setDescription(updatedEntity.getDescription());
    }

    if (updatedEntity.getBannerUrl() != null) {
      server.setBannerUrl(updatedEntity.getBannerUrl());
    }
    
    if (updatedEntity.getIconUrl() != null) {
      server.setIconUrl(updatedEntity.getIconUrl());
    }

    log.info("Server {} successfully updated by user {}", targetServerId, operatorId);
    Server saved = repo.save(server);
    cacheProvider.evict(targetServerId);

    return saved;
  }

  @Override
  @Transactional
  public void deleteServer(UUID operatorId, Long serverId) {
    Server targetServer = repo.findById(serverId)
      .orElseThrow(() -> new IllegalArgumentException("No server with such id!" + serverId));

    if (!targetServer.getOwnerId().equals(operatorId)) {
      throw new AccessDeniedException("Only server owner can delete server");
    }

    repo.delete(targetServer);

    permissionCache.evictServerPermissionAll(serverId);
    permissionCache.evictChannelPermissionsAll(serverId);
    cacheProvider.evict(serverId);
  }

  @Override
  @Transactional
  public void updateServerPositions(UUID operatorId, Map<Long, Integer> serverPositions) {
    if (serverPositions == null || serverPositions.isEmpty()) {
      throw new IllegalArgumentException("Positions cannot be empty!");
    }
    
    Set<Long> inputServers = serverPositions.keySet();
    
    Set<Long> validServers = repo.findServerIdsByUserId(operatorId, inputServers);

    if (validServers.size() != inputServers.size()) {
      throw new IllegalArgumentException("User is not a member of one or more specified servers");
    }
    
    serverPositions.forEach((serverId, position) -> 
      memberRepository.updateMemberPosition(operatorId, serverId, position)
    );

    log.debug("Updated sidebar positions for user {}", operatorId);
  }

  private ServerCacheDto fetchFromDb(Long serverId) {
    return repo.findById(serverId)
      .map(mapper::toCacheDto)
      .orElseThrow(() -> new IllegalArgumentException("No server with id: " + serverId));
  }

  @Override
  public void incrementMemberCounter(Long serverId) {
    if (serverId == null) return;
    repo.incrementMemberCount(serverId);
  }

  @Override
  public void decrementMemberCounter(Long serverId) {
    if (serverId == null) return;
    repo.decrementMemberCount(serverId);
  }
}
