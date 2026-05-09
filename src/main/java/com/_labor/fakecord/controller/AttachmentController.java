package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.UUID;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;

import com._labor.fakecord.domain.dto.PrepareAttachmentRequest;
import com._labor.fakecord.domain.dto.UploadResponse;
import com._labor.fakecord.infrastructure.storage.MediaService;
import com._labor.fakecord.services.AttachmentService;

import software.amazon.awssdk.transfer.s3.model.UploadRequest;


@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {
  
  private final AttachmentService attachmentService;
  private final MediaService mediaService;

  @DeleteMapping
  public ResponseEntity<Void> deleteAttachment(
    @RequestBody List<UUID> attachmentIds,
    Principal principal
  ) {
    UUID userId = UUID.fromString(principal.getName());
    attachmentService.deleteAttachments(attachmentIds, userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/prepare")
  public ResponseEntity<UploadResponse> prepareAttachment(
    @RequestBody PrepareAttachmentRequest request,
    Principal principal
  ) {
    UUID userId = UUID.fromString(principal.getName());

    UploadResponse response = mediaService.prepareAttachment(
      userId,
      request.fileName(),
      request.contentType(),
      request.fileSize()
    );

    return ResponseEntity.ok(response);
  }
}