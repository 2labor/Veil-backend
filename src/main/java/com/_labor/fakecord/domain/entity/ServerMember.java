package com._labor.fakecord.domain.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@NoArgsConstructor
@Getter
@Setter
@Table(name = "server_member")
public class ServerMember {
  
  @EmbeddedId
  private ServerMemberId id;
  
  @Column(name = "user_local_name")
  private String userLocalName;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @Builder
  public ServerMember(ServerMemberId id, String userLocalName, Instant joinedAt) {
    this.id = id;
    this.userLocalName = userLocalName;
    this.joinedAt = Instant.now();
  }  
}
