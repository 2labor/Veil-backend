package com._labor.fakecord.domain.entity;

import java.util.UUID;

import com._labor.fakecord.domain.enums.AttachmentStatus;
import com._labor.fakecord.domain.enums.AttachmentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "attachment")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {
  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "message_id")
  private Message message;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @Column(name = "file_name", nullable = false)
  private String fileName;

  @Column(name = "storage_name", nullable = false)
  private String storageName;

  @Column(name = "content_type", nullable = false)
  private String contentType;

  @Column(name = "file_size")
  private Long fileSize;

  private String thumbnailUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "attachment_status", nullable = false)
  private AttachmentStatus attachmentStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "attachment_type", nullable = false)
  private AttachmentType attachmentType;

  @Column(columnDefinition = "jsonb")
  private String metadata;
}
