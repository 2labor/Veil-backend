package com._labor.fakecord.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.domain.entity.ChannelMemberId;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId>{
  boolean existsById_ChannelIdAndId_UserId(Long channelId, UUID userId);
  Optional<ChannelMember> findById_ChannelIdAndId_UserId(Long channelId, UUID userId);
}
