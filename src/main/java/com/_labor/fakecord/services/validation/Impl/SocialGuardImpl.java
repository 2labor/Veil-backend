package com._labor.fakecord.services.validation.Impl;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ChannelAccessInfo;
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


  @Override
  public void validateInteraction(ChannelAccessInfo accessInfo, UUID authorId) {
    if (ChannelType.DM.equals(accessInfo.getChannelType())) {
      UUID recipientId = accessInfo.getRecipientId();

      if (recipientId != null && (userBlockService.isBlocked(authorId, recipientId) 
          || userBlockService.isBlocked(recipientId, authorId))) {
        log.warn("Blocked message attempt in DM between {} and {}", authorId, recipientId);
        throw new AccessDeniedException("INTERACTION_BLOCKED");
      }
    }
  }
  
}
