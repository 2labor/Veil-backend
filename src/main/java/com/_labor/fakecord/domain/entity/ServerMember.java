package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "member_roles", 
    joinColumns = {
      @JoinColumn(name = "member_user_id", referencedColumnName = "user_id"),
      @JoinColumn(name = "member_server_id", referencedColumnName = "server_id")
    },
    inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
  )
  private Set<ServerRole> roles = new HashSet<>();

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @Builder
  public ServerMember(ServerMemberId id, String userLocalName, Instant joinedAt) {
    this.id = id;
    this.userLocalName = userLocalName;
    this.roles = new HashSet<>();
    this.joinedAt = Instant.now();
  }  

  public void addRole(ServerRole role) {
    this.roles.add(role);
  }

  public void removeRole(ServerRole role) {
    this.roles.remove(role);
  }
}
