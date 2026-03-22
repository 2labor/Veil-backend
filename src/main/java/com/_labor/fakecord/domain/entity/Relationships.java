package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.UUID;

import com._labor.fakecord.domain.enums.RelationshipStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "relationships", indexes = {
  @Index(name = "idx_rel_user_status", columnList = "user_id, status"),
  @Index(name = "idx_rel_target_status", columnList = "target_id, status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_relationships_user_target", columnNames = {"user_id", "target_id"})
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Relationships {
  
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "target_id", nullable = false)
  private User target;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private RelationshipStatus status;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  public void onCreate() {
    Instant now = Instant.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = Instant.now();
  } 
}
