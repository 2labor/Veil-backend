package com._labor.fakecord.domain.notifications;

import java.util.UUID;

import com._labor.fakecord.domain.enums.NotificationPriority;
import com._labor.fakecord.domain.enums.NotificationType;

public record SystemNotification<T extends NotificationPayload>(
  UUID notificationId,
  NotificationType type,
  NotificationPriority priority,
  T data,
  Long timestamp
) {

  public static <T extends NotificationPayload> SystemNotification<T> of(
    NotificationType type,
    NotificationPriority priority,
    T data
  ) {
    return new SystemNotification<>(
      UUID.randomUUID(),
      type,
      priority,
      data,
      System.currentTimeMillis()
    );
  }
}
