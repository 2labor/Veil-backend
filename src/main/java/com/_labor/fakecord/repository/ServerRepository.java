package com._labor.fakecord.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com._labor.fakecord.domain.entity.Server;
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
  @Modifying
  @Query("UPDATE Server s SET s.memberCounter = s.memberCounter + 1 WHERE s.id = :serverId")
  int incrementMemberCount(@Param("serverId") Long serverId);

  @Modifying
  @Query("UPDATE Server s SET s.memberCounter = s.memberCounter - 1 WHERE s.id = :serverId AND s.memberCounter > 0")
  int decrementMemberCount(@Param("serverId") Long serverId);
}