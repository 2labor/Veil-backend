package com._labor.fakecord.services.validation.Impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.repository.ChannelRepository;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.PermissionService;
import com._labor.fakecord.services.validation.ChannelAccessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelAccessValidatorImpl implements ChannelAccessValidator {

  private final ChannelMemberService memberService;
  private final ChannelRepository channelRepository;
  private final PermissionService permissionService;

  @Override
  public void accessValidation(Long channelId, UUID userId) {
    Channel channel = channelRepository.findById(channelId)
      .orElseThrow(() -> new IllegalArgumentException("No channel with such id: " + channelId));
      
    if (channel.getType().isGuildType()) {
      permissionService.requestChannelPermission(userId, channel.getServerId(), channelId, ServerRolePermissions.READ_CHANNEL);
    } else {
      if (!memberService.isMember(channelId, userId)) {
        log.warn("Access Denied: User {} is not a member of channel {}", userId, channelId);
        throw new RuntimeException("ACCESS_DENIED_TO_CHANNEL");
      }
    }
  }
}