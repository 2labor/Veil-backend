package com._labor.fakecord.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.ServerRole;

public interface ServerRolesRepository extends JpaRepository<ServerRole, Long> {
  List<ServerRole> findByServerId(Long serverId);
}
