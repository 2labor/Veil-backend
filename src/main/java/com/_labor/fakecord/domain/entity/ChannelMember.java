package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "channel_members", indexes = {
  @Index(name = "idx_member_user_id", columnList = "user_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelMember {

  @EmbeddedId
  private ChannelMemberId id;

  @Column(name = "last_read_message_id")
  private Long lastReadMessageId;

  @Column(name = "join_at", nullable = false, updatable = false)
  private Instant joinAt;

  @Builder
  public ChannelMember(Long channelId, UUID userId) {
    this.id = new ChannelMemberId(channelId, userId);
    this.joinAt = Instant.now();
    this.lastReadMessageId = 0L;
  }
}
