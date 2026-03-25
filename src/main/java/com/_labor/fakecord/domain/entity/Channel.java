package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com._labor.fakecord.domain.enums.ChannelType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter  
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "channels")
public class Channel {

  @Id
  private Long id;

  @Column(name = "server_id")
  private Long serverId;
  
  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ChannelType type;

  @Column(name = "name")
  private String name;

  @Column(name = "position")
  private Integer position;

  @Column(name = "owner_id")
  private UUID ownerId;

  @Column(name = "last_message_id")
  private Long lastMessageId;

  @Column(name = "last_activity_at")
  private Instant lastActivityAt;

  @Column(name = "last_message_content", length = 255)
  private String lastMessageContent;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Builder
  public Channel(Long id, Long serverId, String name, Instant lastActivityAt, ChannelType type, UUID ownerId, Long lastMessageId, String lastMessageContent) {
    this.id = Objects.requireNonNull(id);
    
    this.serverId = serverId;
    this.name = name;
    this.lastActivityAt = lastActivityAt;
    this.type = type;
    this.ownerId = ownerId;
    this.lastMessageId = lastMessageId;
    this.lastMessageContent = lastMessageContent;
    this.createdAt = (createdAt != null) ? createdAt : Instant.now();
  }
}
