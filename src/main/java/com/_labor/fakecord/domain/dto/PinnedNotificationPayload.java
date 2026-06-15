package com._labor.fakecord.domain.dto;

import com._labor.fakecord.domain.notifications.NotificationPayload;

public record PinnedNotificationPayload(
  String messageId,
  String channelId,
  String textContent,
  boolean hashAttachment,
  String firstAttachmentUrl
) implements NotificationPayload {}