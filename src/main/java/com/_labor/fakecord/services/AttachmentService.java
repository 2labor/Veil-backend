package com._labor.fakecord.services;

import java.util.List;
import java.util.UUID;

import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.entity.Message;

public interface AttachmentService {
  List<Attachment> linkAttachmentsToMessage(Message message, List<UUID> attachmentIds, UUID ownerId);
  List<Attachment> getAttachmentByMessageId(Long messageId);
  void deleteAttachments(List<UUID> attachmentIds, UUID requesterId);
}
