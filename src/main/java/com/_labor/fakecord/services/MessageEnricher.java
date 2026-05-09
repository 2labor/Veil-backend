package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.dto.MessageContext;
import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.ReplyPreviewDto;
import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.infrastructure.outbox.domain.payload.MediaTaskPayload;
import com._labor.fakecord.infrastructure.outbox.domain.payload.MessageCreatedPayload;

public interface MessageEnricher {
  MessageDto enricher(Message message);
  List<MessageDto> enrichBatch(List<Message> messages);
  MessageCreatedPayload createPayload(Message saved, MessageContext context, String authorName);
  ReplyPreviewDto buildReplyPreview(Message parent);
  MediaTaskPayload createMediaTaskPayload(UUID ownerId, List<Attachment> attachments);
}
