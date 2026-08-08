package com._labor.fakecord.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com._labor.fakecord.domain.entity.ServerInvite;

public interface ServerInviteRepository extends JpaRepository<ServerInvite, String> {
  List<ServerInvite> findByServerId(Long serverId);

  @Modifying
  @Query("UPDATE ServerInvite i " +
    "SET i.countUsed = COALESCE(i.countUsed, 0) + 1 " +
    "WHERE i.code = :code " +
    "AND (i.maxUsed IS NULL OR i.countUsed < i.maxUsed) " +
    "AND (i.expiredAt IS NULL OR i.expiredAt > CURRENT_INSTANT)")
  int incrementUsesCount(@Param("code") String code);
}
