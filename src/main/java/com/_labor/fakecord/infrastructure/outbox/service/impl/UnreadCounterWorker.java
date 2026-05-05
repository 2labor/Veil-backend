package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.enums.NotificationPriority;
import com._labor.fakecord.domain.enums.NotificationType;
import com._labor.fakecord.domain.notifications.SystemNotification;
import com._labor.fakecord.infrastructure.outbox.domain.payload.MessageCreatedPayload;
import com._labor.fakecord.infrastructure.presence.PresenceService;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.NotificationService;
import com._labor.fakecord.services.UnreadCounterService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class UnreadCounterWorker {
  
  private final UnreadCounterService counterService;
  private final ChannelMemberService channelMemberService;
  private final PresenceService presenceService;
  private final NotificationService notificationService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "chat.messages", groupId = "unread-counters-group")
  public void handleMessageCreated(String raw) {
    try {
      MessageCreatedPayload basePayload = objectMapper.readValue(raw, MessageCreatedPayload.class);
      
      List<UUID> memberIds = channelMemberService.getMemberIds(basePayload.channelId());
      
    memberIds.stream()
      .filter(userId -> !userId.equals(basePayload.authorId()))
      .filter(userId -> !presenceService.isUserInChannel(userId, basePayload.channelId()))
      .forEach(userId -> {
        counterService.increment(basePayload.channelId(), userId);
        
        int currentUnread = counterService.getCounter(basePayload.channelId(), userId);
        long now = System.currentTimeMillis();
        MessageCreatedPayload personalPayload = new MessageCreatedPayload(
          basePayload.messageId(),
          basePayload.channelId(),
          basePayload.serverId(),
          basePayload.authorId(),
          basePayload.authorName(),
          basePayload.content(),
          basePayload.parentId(),
          basePayload.attachments(),
          currentUnread,
          now,
          basePayload.channelType(),
          basePayload.channelName()
        );

        var notification = SystemNotification.of(
          basePayload.channelId(),
          null,
          NotificationType.MESSAGE_CREATE,
          NotificationPriority.HIGH,
          personalPayload
        );

        notificationService.sendToUser(userId, notification);
    });
  } catch (Exception e) {
    log.error("Worker error", e);
  }
}

}
