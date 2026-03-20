package com._labor.fakecord.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.entity.Channel;

public interface ChannelRepository extends JpaRepository<Channel, Long>{
  List<Channel> findAllByServerIdOrderByPositionAsc(Long serverId);

  @Modifying
  @Query("UPDATE Channel c SET c.position = :position WHERE c.id = :id")
  void updatePosition(Long id, Integer position);
}
