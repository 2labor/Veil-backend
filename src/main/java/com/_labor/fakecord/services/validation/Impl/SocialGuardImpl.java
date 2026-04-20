package com._labor.fakecord.services.validation.Impl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.repository.ChannelRepository;
import com._labor.fakecord.services.UserBlockService;
import com._labor.fakecord.services.validation.SocialGuard;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocialGuardImpl implements SocialGuard {

  private final UserBlockService userBlockService;
  private final ChannelRepository channelRepository;


  @Override
  public void validateInteraction(Long channelId, UUID authorId) {
    channelRepository.findById(channelId).ifPresent(channel -> {
      if (channel.getType() == ChannelType.DM);
    });
  }
  
}
