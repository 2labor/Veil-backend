package com._labor.fakecord.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class StatusController {
  
  @MessageMapping("/presence/touch")
  public void touch() {
    log.trace("Received presence touch from client");
  }

}
