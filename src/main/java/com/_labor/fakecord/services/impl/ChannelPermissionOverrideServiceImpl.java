package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.entity.ChannelPermissionOverride;
import com._labor.fakecord.domain.request.SetChannelOverrideRequest;
import com._labor.fakecord.infrastructure.cache.services.PermissionCache;
import com._labor.fakecord.repository.ChannelPermissionOverrideRepository;
import com._labor.fakecord.services.ChannelPermissionOverrideService;
import com._labor.fakecord.services.PermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChannelPermissionOverrideServiceImpl implements ChannelPermissionOverrideService {

  private final ChannelPermissionOverrideRepository repository;
  private final PermissionService permissionService;
  private final PermissionCache permissionCache;
  
  @Override
  @Transactional(readOnly = true)
  public List<ChannelPermissionOverride> getChannelPermissionsOverride(UUID operatorId, Long serverId, Long channelId) {
    return repository.findByChannelId(channelId);
  }

  @Override
  @Transactional
  public ChannelPermissionOverride setChannelPermissionOverride(UUID operatorId, Long serverId, Long channelId, String holderId, SetChannelOverrideRequest request) {
    long combinedTargetMask = request.allowMask() | request.denyMask();
    permissionService.requireCanGrantPermissions(operatorId, serverId, combinedTargetMask);

    ChannelPermissionOverride override = repository.findByChannelIdAndHolderIdAndHolderType(channelId, holderId, request.holderType())
      .orElseGet(() -> ChannelPermissionOverride.builder()
        .channelId(channelId)
        .holderId(holderId)
        .holderType(request.holderType())
        .build()
    );

    override.setAllowMask(request.allowMask());
    override.setDenyMask(request.denyMask());

    permissionCache.evictChannelPermissionsAll(serverId);
    return repository.save(override);
  }

  @Override
  @Transactional
  public void deleteChannelPermissionOverride(UUID operatorId, Long serverId, Long channelId, String holderId) {
    repository.deleteByChannelIdAndHolderId(channelId, holderId);
    permissionCache.evictChannelPermissionsAll(serverId);
  }
  
}
