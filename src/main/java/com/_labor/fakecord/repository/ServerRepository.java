package com._labor.fakecord.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.entity.Server;

import io.lettuce.core.dynamic.annotation.Param;

public interface ServerRepository extends JpaRepository<Server, Long>{
  boolean existsByIdAndOwnerId(Long id, UUID ownerId);
  @Query("""
    SELECT s FROM Server s 
    JOIN ServerMember sm ON s.id = sm.id.serverId 
    WHERE sm.id.userId = :userId 
    ORDER BY sm.position ASC
    """)
  List<Server> findAllByUserIdOrderedByPosition(@Param("userId") UUID userId);
  @Query("""
    SELECT sm.id.serverId 
    FROM ServerMember sm 
    WHERE sm.id.userId = :userId AND sm.id.serverId IN :serverIds
  """)
  Set<Long> findServerIdsByUserId(@Param("userId") UUID userId, @Param("serverIds") Set<Long> serverIds);
}