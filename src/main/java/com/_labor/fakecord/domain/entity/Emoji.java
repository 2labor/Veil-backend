package com._labor.fakecord.domain.entity;

import java.time.Instant;

import com._labor.fakecord.domain.enums.EmojiCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "emojis",
  uniqueConstraints = {
    @UniqueConstraint(name = "uc_server_emoji_name", columnNames = {"server_id", "name"})
  },
  indexes = {
    @Index(name = "idx_emojis_server_id", columnList = "service_id")
  }
)
public class Emoji {
  @Id
  private Long id;

  @Column(name = "name", nullable = false, length = 32)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  private EmojiCategory category;

  @Column(name = "server_id")
  private Long serverId;

  @Column(name = "url")
  private String url;

  @Column(name = "is_animated", nullable = false)
  private boolean isAnimated;

  @Column(name = "created_at")
  private Instant createdAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
  }
}
