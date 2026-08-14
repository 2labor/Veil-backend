package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.entity.ChannelPermissionOverride;
import com._labor.fakecord.domain.request.SetChannelOverrideRequest;

public interface ChannelPermissionOverrideService {
  List<ChannelPermissionOverride> getChannelPermissionsOverride(UUID operatorId, Long serverId, Long channelId);
  ChannelPermissionOverride setChannelPermissionOverride(UUID operatorId, Long serverId, Long channelId, String holderId, SetChannelOverrideRequest request);
  void deleteChannelPermissionOverride(UUID operatorId, Long serverId, Long channelId, String holderId);
}