package com._labor.fakecord.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;

@Repository
public interface ServerMemberRepository extends JpaRepository<ServerMember, ServerMemberId> {
  boolean existsById_ServerIdAndId_UserId(Long serverId, UUID userId);
  // ServerMember findByUserId(UUID userId);
  // ServerMember findByServerId(Long serverId);
}