package com._labor.fakecord.services;

import java.util.UUID;

import com._labor.fakecord.domain.notifications.NotificationPayload;
import com._labor.fakecord.domain.notifications.SystemNotification;

public interface NotificationService {
  <T extends NotificationPayload> void sendToUser(UUID userId, SystemNotification<T> notification);
  <T extends NotificationPayload> void sendToChannel(Long channelId, SystemNotification<T> notification);
  <T extends NotificationPayload> void broadcastPresence(Long channelId, SystemNotification<T> notification);
}
