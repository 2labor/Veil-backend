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
@Table(name = "server_invite", indexes = {
  @Index(name = "idx_invite_code", columnList = "code", unique = true),
  @Index(name = "idx_invite_server", columnList = "server_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerInvite {
  
  @Id
  @Column(name = "code", nullable = false, length = 16)
  private String code;

  @Column(name = "server_id", nullable = false)
  private Long serverId;

  @Column(name = "creator_id", nullable = false)
  private UUID creatorId;

  @Column(name = "max_used")
  private Integer maxUsed;

  @Column(name = "count_used")
  private Integer countUsed;

  @Column(name = "expired_at")
  private Instant expiredAt;

  @Column(name = "created_at")
  private Instant createdAt;

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
  }

  public boolean isExpired() {
    if (this.expiredAt != null && Instant.now().isAfter(expiredAt)) return true;
    if (this.maxUsed != null && maxUsed > 0 && countUsed >= maxUsed) return true;
    return false;
  }

}
