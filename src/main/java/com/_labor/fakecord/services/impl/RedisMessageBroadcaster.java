package com._labor.fakecord.services.impl;

import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.MessageDelete;
import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.NotificationPriority;
import com._labor.fakecord.domain.enums.NotificationType;
import com._labor.fakecord.domain.enums.SocketEventType;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.MessageMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.domain.notifications.NotificationPayload;
import com._labor.fakecord.domain.notifications.SystemNotification;
import com._labor.fakecord.services.MessageBroadcaster;
import com._labor.fakecord.services.NotificationService;
import com._labor.fakecord.services.TypingService;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisMessageBroadcaster implements MessageBroadcaster {
  
  private final NotificationService notificationService;
  private final MessageMapper messageMapper;
  private final UserProfileCache profileCache;
  private final UserProfileMapper profileMapper;
  private final TypingService typingService;

  @Override
  public void broadcastMessageEvent(Message message, SocketEventType type) {
    NotificationPriority priority = (type == SocketEventType.MESSAGE_CREATE) 
      ? NotificationPriority.HIGH : NotificationPriority.LOW;

    broadcastMessageEvent(message, type, priority);
  }

  public void broadcastMessageEvent(Message message, SocketEventType type, NotificationPriority priority) {
    log.debug("Broadcasting message event [{}] with priority {}", type, priority);

    MessageDto dto = convertToDto(message);

    SystemNotification<MessageDto> notification = SystemNotification.of(
      message.getChannelId(),
      message.getId(),
      NotificationType.valueOf(type.name()), 
      priority, 
      dto
    );
    
    typingService.stopTyping(message.getChannelId(), message.getAuthorId());
    
    notificationService.sendToChannel(message.getChannelId(), notification);
  }

  @Override
  public void broadcastDeletion(Long channelId, Long messageId) {
    SystemNotification<MessageDelete> notification = SystemNotification.of(
      channelId, 
      messageId, 
      NotificationType.MESSAGE_DELETE, 
      NotificationPriority.LOW, 
      new MessageDelete(messageId)
    );

    notificationService.sendToChannel(channelId, notification);
  }

  @Override
  public void broadcastSystemEvent(Long channelId, SocketEventType type, Object data) {
    broadcastSystemEvent(channelId, type, data, NotificationPriority.LOW);
  }

  public void broadcastSystemEvent(Long channelId, SocketEventType type, Object data, NotificationPriority priority) {
    log.debug("Broadcasting system event [{}] for channel {}", type, channelId);

    if (!(data instanceof NotificationPayload payload)) {
      log.error("Data for system event must implement NotificationPayload. Got: {}", 
      data != null ? data.getClass().getSimpleName() : "null");
      return;
    }

    SystemNotification<NotificationPayload> notification = SystemNotification.of(
      channelId,
      null,
      NotificationType.valueOf(type.name()),
      NotificationPriority.LOW,
      payload
    );

    notificationService.sendToChannel(channelId, notification);
  }

  private MessageDto convertToDto(Message message) {
    var fullProfile = profileCache.getUserProfile(message.getAuthorId());
    var shortProfile = profileMapper.toShortDto(fullProfile, UserStatus.ONLINE);
    return messageMapper.toDto(message, shortProfile);
  }
}