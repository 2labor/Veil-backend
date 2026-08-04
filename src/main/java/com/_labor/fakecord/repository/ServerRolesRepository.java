package com._labor.fakecord.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.entity.ServerRole;

public interface ServerRolesRepository extends JpaRepository<ServerRole, Long> {
  List<ServerRole> findByServerId(Long serverId);
  @Query("SELECT COALESCE(MAX(r.position), 0) FROM ServerRole r WHERE r.serverId = :serverId")
  Integer findMaxPositionByServerId(Long serverId);
  List<ServerRole> findByServerIdOrderByPositionDesc(Long serverId);
}
