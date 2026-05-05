package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com._labor.fakecord.domain.enums.MessageType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

@Entity
@Getter
@Setter
@Table(name = "messages", indexes = {
  @Index(name = "idx_messages_channel_pagination", columnList = "channel_id, id DESC")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {
  
  @Id
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private MessageType type;
  
  @Column(name = "content")
  private String content;

  @Column(name = "channel_id", nullable = false)
  private Long channelId;

  @Column(name = "author_id", nullable = false)
  private UUID authorId;

  @Column(name = "parent_id")
  private Long parentId;

  @Column(name = "nonce", unique = true)
  private String nonce;

  @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attachment> attachments = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Builder
  public Message(Long id, MessageType type, Long channelId, UUID authorId, Long parentId, String content, String nonce, List<Attachment> attachments) {
    Objects.requireNonNull(id, "ID (TSID) must be provided");
    Objects.requireNonNull(channelId, "Channel ID is required");
    Objects.requireNonNull(authorId, "Author ID is required");

    this.id = id;
    this.type = (type != null) ? type : MessageType.TEXT;
    this.channelId = channelId;
    this.authorId = authorId;
    this.parentId = parentId;
    this.content = content;
    this.nonce = nonce;

    if (attachments != null) {
      for (Attachment attach : attachments) {
        this.attachments.add(attach);
      }
    }

    this.createdAt = Instant.now();
  }

   public void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public void addAttachment(Attachment attachment) {
    this.attachments.add(attachment);
    attachment.setMessage(this);
  }
}
