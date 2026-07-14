package com._labor.fakecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.entity.Server;

import io.lettuce.core.dynamic.annotation.Param;

public interface ServerRepository extends JpaRepository<Server, Long>{
  @Query("SELECT s FROM Server s " +
    "JOIN ServerMember sm ON s.id = sm.id.serverId " +
    "WHERE sm.id.userId = :userId")
    List<Server> findByUserId(@Param("userId") UUID userId);
}