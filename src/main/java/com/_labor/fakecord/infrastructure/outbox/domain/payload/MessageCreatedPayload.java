package com._labor.fakecord.infrastructure.outbox.domain.payload;

import java.util.UUID;

import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.domain.notifications.NotificationPayload;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public record MessageCreatedPayload(
    @JsonSerialize(using = ToStringSerializer.class)
    Long messageId, 
    @JsonSerialize(using = ToStringSerializer.class)
    Long channelId, 
    Long serverId,
    UUID authorId,
    String authorName,
    String content, 
    Integer unreadCount, 
    Long timestamp,
    ChannelType channelType,
    String channelName
) implements NotificationPayload {}
