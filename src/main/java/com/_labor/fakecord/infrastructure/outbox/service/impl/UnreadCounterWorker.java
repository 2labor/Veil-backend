package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.outbox.domain.payload.MessageCreatedPayload;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.UnreadCounterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UnreadCounterWorker {
  
  private final UnreadCounterService counterService;
  private final ChannelMemberService channelMemberService;

  @KafkaListener(
    topics = "chat:messages",
    groupId = "unread-counters-group"
  )
  public void handleMessageCreated(MessageCreatedPayload payload) {
    log.debug("Processing unread counters for channel {}", payload.channelId());

    List<UUID> memberIds = channelMemberService.getMemberIds(payload.channelId());

    memberIds.stream()
      .filter(userId -> !userId.equals(payload.authorId()))
      .forEach(userId -> counterService.increment(payload.channelId(), userId));
  }

}
