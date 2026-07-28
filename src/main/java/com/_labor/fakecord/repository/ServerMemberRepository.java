package com._labor.fakecord.repository;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerMemberId;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface ServerMemberRepository extends JpaRepository<ServerMember, ServerMemberId> {
  boolean existsById_ServerIdAndId_UserId(Long serverId, UUID userId);
  @Query("SELECT DISTINCT sm FROM ServerMember sm " +
    "LEFT JOIN FETCH sm.roles r " +
    "WHERE sm.id.serverId = :serverId " +
    "ORDER BY sm.userLocalName ASC")
  Slice<ServerMember> findAllByServerIdWithRoles(@Param("serverId") Long serverId, Pageable pageable);
  // ServerMember findByUserId(UUID userId);
  // ServerMember findByServerId(Long serverId);
}