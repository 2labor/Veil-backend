package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.AttachmentStatus;
import com._labor.fakecord.repository.AttachmentRepository;
import com._labor.fakecord.services.AttachmentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

  private final AttachmentRepository repository;

  @Override
  public List<Attachment> linkAttachmentsToMessage(Message message, List<UUID> attachmentIds, UUID ownerId) {
    List<Attachment> attachments = repository.findAllById(attachmentIds);

    if (attachments.size() != attachmentIds.size()) {
      throw new EntityNotFoundException("One or more attachments not found");
    }

    for (Attachment attach : attachments) {
      if (!attach.getOwnerId().equals(ownerId)) {
        throw new AccessDeniedException("User does not own the attachment: " + attach.getId());
      }

      attach.setMessage(message);
      attach.setAttachmentStatus(AttachmentStatus.ACTIVE);
      
      if (message.getAttachments() != null) {
        message.addAttachment(attach);
      }
    }

    log.info("Successfully linked {} attachments to message {}", attachments.size(), message.getId());
    return repository.saveAll(attachments);
  }

  @Override
  public void deleteAttachments(List<UUID> attachmentIds, UUID requesterId) {
    if (attachmentIds == null || attachmentIds.isEmpty()) return;
    
    log.info("Processing batch deletion for {} attachments by user {}", attachmentIds.size(), requesterId);

    List<Attachment> attachments = repository.findAllById(attachmentIds   );

    for (Attachment attachment : attachments) {
      if (!attachment.getOwnerId().equals(requesterId)) {
        log.warn("Unauthorized deletion attempt: user {} on attachment {}", requesterId, attachment.getId());
        throw new AccessDeniedException("You don't have permission to delete one or more of these files");
      }
    }

    attachments.forEach(attachment -> {
      attachment.setAttachmentStatus(AttachmentStatus.DELETE);

      if (attachment.getMessage() != null) {
        attachment.getMessage().getAttachments().remove(attachment);
        attachment.setMessage(null);
      }
    });

    repository.saveAll(attachments);

    log.debug("Marked {} attachments as DELETED", attachments.size());
  }

  @Override
  public List<Attachment> getAttachmentByMessageId(Long messageId) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'getAttachmentByMessageId'");
  }

  
}
