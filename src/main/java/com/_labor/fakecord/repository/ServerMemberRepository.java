package com._labor.fakecord.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;

public interface ServerMemberRepository extends JpaRepository<ServerMember, ServerMemberId> {
  // ServerMember findByUserId(UUID userId);
  // ServerMember findByServerId(Long serverId);
}