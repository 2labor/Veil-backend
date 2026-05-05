package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.UUID;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;

import com._labor.fakecord.services.AttachmentService;


@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {
  
  private final AttachmentService attachmentService;

  @DeleteMapping
  public ResponseEntity<Void> deleteAttachment(
    @RequestBody List<UUID> attachmentIds,
    Principal principal
  ) {
    UUID userId = UUID.fromString(principal.getName());
    attachmentService.deleteAttachments(attachmentIds, userId);
    return ResponseEntity.noContent().build();
  }

}