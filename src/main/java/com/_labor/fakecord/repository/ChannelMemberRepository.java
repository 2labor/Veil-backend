package com._labor.fakecord.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.domain.entity.ChannelMemberId;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, ChannelMemberId>{
  Slice<ChannelMember> findAllById_ChannelId(Long channelId, Pageable pageable);
  boolean existsById_ChannelIdAndId_UserId(Long channelId, UUID userId);
  Optional<ChannelMember> findById_ChannelIdAndId_UserId(Long channelId, UUID userId);
  void deleteById_ChannelIdAndId_UserId(Long channelId, UUID userId); 
}
