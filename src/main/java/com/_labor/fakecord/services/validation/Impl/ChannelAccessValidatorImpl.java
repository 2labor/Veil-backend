package com._labor.fakecord.services.validation.Impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.validation.ChannelAccessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChannelAccessValidatorImpl implements ChannelAccessValidator {

  private final ChannelMemberService memberService;

  @Override
  public void accessValidation(Long channelId, UUID userId) {
    if (!memberService.isMember(channelId, userId)) {
      log.warn("Access Denied: User {} is not a member of channel {}", userId, channelId);
      throw new RuntimeException("ACCESS_DENIED_TO_CHANNEL");
    }
  }
  
}
