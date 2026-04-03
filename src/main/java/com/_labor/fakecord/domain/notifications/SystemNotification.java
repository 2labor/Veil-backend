package com._labor.fakecord.domain.notifications;

import java.util.UUID;

import com._labor.fakecord.domain.enums.NotificationPriority;
import com._labor.fakecord.domain.enums.NotificationType;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record SystemNotification<T extends NotificationPayload>(
  UUID notificationId,
  @JsonSerialize(using = ToStringSerializer.class)
  Long channelId, 
  @JsonSerialize(using = ToStringSerializer.class)
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
