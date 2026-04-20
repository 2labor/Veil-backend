package com._labor.fakecord.services.validation;

import java.util.UUID;

import com._labor.fakecord.domain.entity.Channel;

public interface SocialGuard {
  void validateInteraction(Long channelId, UUID authorId);
}
