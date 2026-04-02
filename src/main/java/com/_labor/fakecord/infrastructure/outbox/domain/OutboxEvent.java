package com._labor.fakecord.infrastructure.outbox.domain;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable =  false)
  private OutboxEventType type; 

  @Column(name = "aggregate_id", nullable = false)
  private String aggregateId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private EventStatus status;

  @Column(name = "retry_count", nullable = false)
  private int retryCount = 0;

  @Column(name = "create_at", nullable = false)
  private Instant createAt;

  @Column(name = "process_at")
  private Instant processAt;

  @Column(name = "error_log")
  private String errorLog;

  @PrePersist 
  public void onCreate() {
    this.createAt = Instant.now();
    if (null == status) {
      this.status = EventStatus.PENDING;
    }
  }
}
