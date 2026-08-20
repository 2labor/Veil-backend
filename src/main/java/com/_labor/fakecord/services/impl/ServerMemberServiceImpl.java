package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.ServerMemberSidebarResponseDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.mappper.ServerMemberMapper;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.payload.ServerMemberEventPayload;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.ServerMemberRepository;
import com._labor.fakecord.repository.ServerRolesRepository;
import com._labor.fakecord.services.ServerMemberService;
import com._labor.fakecord.services.ServerSecurityService;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerMemberServiceImpl implements ServerMemberService {

  private final ServerMemberRepository repository;
  private final ServerRolesRepository roleRepository;
  private final ServerMemberMapper mapper;
  private final UserProfileCache userProfileService;
  private final OutboxService outboxService;
  private final ServerSecurityService serverService;

  @Override
  public boolean checkIsUserMember(Long serverId, UUID userId) {
    return repository.existsByIdServerIdAndIdUserId(serverId, userId);
  }

  @Override
  public Slice<ServerMemberSidebarResponseDto> getServerMembers(Long serverId, UUID currentId, Pageable pageable) {
    if (!checkIsUserMember(serverId, currentId)){
      throw new AccessDeniedException("You do not have access to this server!");
    }

    Slice<ServerMember> members = repository.findAllByServerIdWithRoles(serverId, pageable);

    List<UUID> userIds = members.stream().map(m -> m.getId().getUserId()).toList();

    Map<UUID, UserProfileShort> userProfiles = userProfileService.getUserProfileShortBatch(userIds);

    List<ServerMemberSidebarResponseDto> dtos = mapper.toSidebarDtos(
      members.getContent(), 
      userProfiles
    );

    return new SliceImpl<ServerMemberSidebarResponseDto>(dtos, pageable, members.hasNext());
  }

  @Override
  @Transactional(readOnly = true)
  public ServerMember getMemberWithRoles(UUID operatorId, UUID userId, Long serverId) {
    if (!checkIsUserMember(serverId, operatorId)) throw new AccessDeniedException("You have to be member of server for using this functionality!");

    ServerMemberId id = new ServerMemberId(userId, serverId);
    return repository.findByIdWithRoles(id)
      .orElseThrow(() -> new AccessDeniedException("User is not a member of this server"));
  }

  @Transactional(readOnly = true)
  @Override
  public ServerMember getServerMember(UUID userId, Long serverId) {
    ServerMemberId memberId = new ServerMemberId(userId, serverId);

    return repository.findById(memberId)
      .orElseThrow(() -> new IllegalArgumentException("User is not a member of this server"));
  }

  @Override
  public int getMemberMaxRolePosition(UUID operatorId, UUID userId, Long serverId) {
    ServerMember member = getMemberWithRoles(operatorId, userId, serverId);

    if (member.getRoles() == null || member.getRoles().isEmpty()) {
      return 0;
    }

    return member.getRoles().stream()
      .mapToInt(ServerRole::getPosition)
      .max()
      .orElse(0);
  }

  @Override
  @Transactional
  public ServerMember addMemberToServer(UUID userId, Long serverId) {
    ServerMemberId memberId = new ServerMemberId(userId, serverId);

    return repository.findById(memberId).orElseGet(() -> {
      log.info("Adding user {} to server {}", userId, serverId);
      
      ServerRole defaultRole = roleRepository.findByServerIdAndPosition(serverId, 0)
        .orElseThrow(() -> new IllegalArgumentException("No default role on a server!"));
      
      ServerMember newMember = ServerMember.builder()
        .id(memberId)
        .build();
      newMember.addRole(defaultRole);

      ServerMember saved = repository.save(newMember);

      outboxService.publish(serverId.toString(), OutboxEventType.SERVER_MEMBER_JOINED, new ServerMemberEventPayload(serverId, userId));

      return saved;
    });
  }

  @Override
  @Transactional
  public void removeMemberFromServer(UUID userId, Long serverId) {
    if (!checkIsUserMember(serverId, userId)) {
      throw new IllegalArgumentException("No user with such id: " + userId + "on server: " + serverId);
    }

    if(serverService.isUserOwner(userId, serverId)) {
      throw new IllegalStateException("Server owner cannot leave the server! Transfer ownership or delete the server instead.");
    }

    repository.deleteById(new ServerMemberId(userId, serverId));
    outboxService.publish(serverId.toString(), OutboxEventType.SERVER_MEMBER_LEFT, new ServerMemberEventPayload(serverId, userId));

    log.info("User {} successfully left server {}", userId, serverId);
  }
}
