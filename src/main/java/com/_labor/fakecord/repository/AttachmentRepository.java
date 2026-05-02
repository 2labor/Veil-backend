package com._labor.fakecord.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;

import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.enums.AttachmentStatus;

public interface AttachmentRepository extends JpaRepositoryImplementation<Attachment, UUID> {
  List<Attachment> findAllByIdInAndAttachmentStatus(List<UUID> ids, AttachmentStatus status);
}