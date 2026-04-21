package com._labor.fakecord.services.validation;

import java.util.UUID;

import com._labor.fakecord.domain.dto.ChannelAccessInfo;
import com._labor.fakecord.domain.entity.Channel;

public interface SocialGuard {
  void validateInteraction(ChannelAccessInfo accessInfo, UUID authorId);
}
