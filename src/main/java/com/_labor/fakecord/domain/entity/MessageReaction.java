package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "message_reaction",
  uniqueConstraints = {
    @UniqueConstraint(name = "uc_message_user_emoji", columnNames = {"message_id", "user_id", "emoji_id"})
  },
  indexes = {
    @Index(name = "idx_reactions_message_id", columnList = "message_id")
  }
)
public class MessageReaction {
  
  @Id
  private Long id;

  @Column(name = "message_id", nullable = false)
  private Long messageId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "emoji_id", nullable = false)
  private Long emojiId;

  @Column(name = "created_at")
  private Instant createdAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
  }
}
