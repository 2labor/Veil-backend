package com._labor.fakecord.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "server_roles")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class ServerRole {
  @Id
  private Long id;

  @Column(name = "server_id", nullable = false)
  private Long serverId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "is_displayable", nullable = false)
  private boolean isDisplayable;

  @Column(name = "color_hex", length = 7, nullable = false)
  private String colorHex;

  @Column(name = "permissions", nullable = false)
  private Long permissions;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Builder
  public ServerRole(
    Long id, 
    Long serverId, 
    String name, 
    boolean isDisplayable, 
    String colorHex, 
    Long permissions) 
  {
    this.id = id;
    this.serverId = serverId;
    this.name = name;
    this.isDisplayable = isDisplayable;
    this.colorHex = colorHex;
    this.permissions = permissions;    
  }

  @PrePersist
  public void onCreate() {
    this.createdAt = Instant.now();
    this.updatedAt = Instant.now();
  }

  @PreUpdate
  public void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
