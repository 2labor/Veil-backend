package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com._labor.fakecord.repository.ServerMemberRepository;
import com._labor.fakecord.repository.ServerRepository;
import com._labor.fakecord.services.ChannelService;
import com._labor.fakecord.services.ServerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerServiceImpl implements ServerService {
  private final ServerRepository repo;
  private final ServerMemberRepository memberRepository;
  private final IdGenerator idGenerator;
  private final ChannelService channelService;

  @Override
  @Transactional
  public Server createServer(UUID operatorId, String serverName, String iconUrl) {
    Long serverId = idGenerator.nextId();

    Server server = Server.builder()
      .id(serverId)
      .ownerId(operatorId)
      .name(serverName)
      .iconUrl(iconUrl)
      .build();
    Server savedServer = repo.save(server);
    
    ServerMemberId memberId = new ServerMemberId(operatorId, savedServer.getId());
    ServerMember member = ServerMember.builder()
      .id(memberId)
      .build();
    memberRepository.save(member);

    channelService.createChannel(savedServer.getId(), operatorId, "general", ChannelType.GUILD_TEXT);

    return savedServer;
  }
}
