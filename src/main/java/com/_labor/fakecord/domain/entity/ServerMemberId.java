package com._labor.fakecord.domain.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "server_member_id")
public class ServerMemberId implements Serializable {
  @Column(name = "user_id", nullable = false)
  private UUID userId;
  @Column(name = "server_id", nullable = false)
  private Long serverId;
}
