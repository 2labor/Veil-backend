package com._labor.fakecord.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.ChannelPermissionOverride;
import com._labor.fakecord.domain.enums.PermissionHolderType;

public interface ChannelPermissionOverrideRepository extends JpaRepository<ChannelPermissionOverride, Long>{
  List<ChannelPermissionOverride> findByChannelId(Long channelId);
  List<ChannelPermissionOverride> findByChannelIdIn(List<Long> channelIds);
  Optional<ChannelPermissionOverride> findByChannelIdAndHolderIdAndHolderType(
    Long channelId, 
    String holderId, 
    PermissionHolderType holderType
  );
  void deleteByChannelIdAndHolderId(Long channelId, String holderId);
}
