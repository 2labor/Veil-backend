package com._labor.fakecord.domain.notifications;

import java.util.UUID;

import com._labor.fakecord.domain.enums.NotificationPriority;
import com._labor.fakecord.domain.enums.NotificationType;

public record SystemNotification<T extends NotificationPayload>(
  UUID notificationId,
  Long channelId, 
  Long targetId,
  NotificationType type,
  NotificationPriority priority,
  T data,
  Long timestamp
) {

  public static <T extends NotificationPayload> SystemNotification<T> of(
    Long channelId, 
    Long targetId,
    NotificationType type,
    NotificationPriority priority,
    T data
  ) {
    return new SystemNotification<>(
      UUID.randomUUID(),
      channelId,
      targetId,
      type,
      priority,
      data,
      System.currentTimeMillis()
    );
  }
}
