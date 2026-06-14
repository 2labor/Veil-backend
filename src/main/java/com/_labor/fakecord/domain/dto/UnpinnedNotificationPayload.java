package com._labor.fakecord.domain.dto;

import com._labor.fakecord.domain.notifications.NotificationPayload;

public record UnpinnedNotificationPayload(
  String messageId,
  String channelId,
  PinnedNotificationPayload pinFallback
) implements NotificationPayload {}