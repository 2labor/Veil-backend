package com._labor.fakecord.services.validation;

import java.util.UUID;

import com._labor.fakecord.domain.dto.MessageContext;

public interface SocialGuard {
  void validateInteraction(MessageContext messageContext, UUID authorId);
}
