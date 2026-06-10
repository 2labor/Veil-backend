package com._labor.fakecord.services.validation;

import java.util.List;
import java.util.UUID;

public interface MessageValidator {
  void validate(String content, boolean hasAttachment);
}
