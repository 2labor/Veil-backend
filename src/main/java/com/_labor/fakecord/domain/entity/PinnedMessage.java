package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pinned_message", indexes = {
  @Index(name = "idx_pinned_channel_at", columnList = "channel_id, pinned_at DESC")
})
public class PinnedMessage {

  @Id
  @Column(name = "message_id", nullable = false)
  Long messageId;

  @Column(name = "channel_id", nullable = false)
  Long channelId;

  @Column(name = "pinned_by", nullable = false)
  UUID pinnedBy;

  @Column(name = "pinned_at", nullable = false)
  Instant pinnedAt;

  @PrePersist
  private void onCreate() {
    if (this.pinnedAt == null) {
      this.pinnedAt = Instant.now();
    }
  }
}
