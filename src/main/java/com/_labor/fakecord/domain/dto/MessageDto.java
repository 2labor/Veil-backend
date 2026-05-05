package com._labor.fakecord.domain.dto;

import java.util.List;

import com._labor.fakecord.domain.enums.MessageType;
import com._labor.fakecord.domain.notifications.NotificationPayload;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
public record MessageDto (
  @JsonSerialize(using = ToStringSerializer.class)
  String id,
  String content,
  MessageType type,
  UserProfileShort author,
  Long parentId,
  ReplyPreviewDto previewDto,
  String nonce,
  Long updatesAt,
  List<AttachmentDto> attachments,
  Long timestamp
) implements NotificationPayload {}
