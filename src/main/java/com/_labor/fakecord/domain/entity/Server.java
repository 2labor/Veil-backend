package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.UUID;

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
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="server")
public class Server {

  @Id
  private Long id; 

  @Column(name  = "owner_id")
  private UUID ownerId;
  
  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "banner_url")
  private String bannerUrl;

  @Column(name = "icon_url")
  private String iconUrl;

  @Column(name = "position")
  private Integer position;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Builder
  public Server(Long id, UUID ownerId, String name, String description, String bannerUrl, String iconUrl, Integer position) {
    this.id = id;
    this.ownerId = ownerId;
    this.name = name;
    this.description = description;
    this.bannerUrl = bannerUrl;
    this.iconUrl = iconUrl;
    this.position = position != null ? position : 0;
  }

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