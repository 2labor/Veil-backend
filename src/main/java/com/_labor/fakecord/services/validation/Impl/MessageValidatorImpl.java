package com._labor.fakecord.services.validation.Impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com._labor.fakecord.services.validation.MessageValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageValidatorImpl implements  MessageValidator {

  @Value("${app.validation.chat.max-length}")
  private int MAX_LENGTH; 

  @Override
  public void validate(String content, boolean hasAttachment) {
    boolean hasContent = content != null && !content.isEmpty();

    if (!hasContent && !hasAttachment) {
      throw new IllegalArgumentException("Message is empty, no content inside");
    }

    if (hasContent && content.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("Message is too long. Max allowed: " + MAX_LENGTH);
    }
  }
  
}
