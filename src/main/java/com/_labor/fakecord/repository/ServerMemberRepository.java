package com._labor.fakecord.repository;

import java.util.Optional;
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
  @Query("SELECT DISTINCT sm FROM ServerMember sm " +
    "LEFT JOIN FETCH sm.roles r " +
    "WHERE sm.id.serverId = :serverId " +
    "ORDER BY sm.userLocalName ASC")
  Slice<ServerMember> findAllByServerIdWithRoles(@Param("serverId") Long serverId, Pageable pageable);

  @Query("SELECT sm FROM ServerMember sm " +
    "LEFT JOIN FETCH sm.roles " +
    "WHERE sm.id = :id")
  Optional<ServerMember> findByIdWithRoles(@Param("id") ServerMemberId id);
  boolean existsByIdServerIdAndIdUserId(Long serverId, UUID userId);
  // ServerMember findByUserId(UUID userId);
  // ServerMember findByServerId(Long serverId);
}