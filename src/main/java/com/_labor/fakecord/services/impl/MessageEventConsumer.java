package com._labor.fakecord.services.impl;

import org.springframework.stereotype.Service;

import com._labor.fakecord.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageEventConsumer {
  
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  

}
