package com._labor.fakecord.domain.entity;

import com._labor.fakecord.domain.enums.PermissionHolderType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "channel_permission_override", indexes = {
  @Index(name = "idx_override_channel", columnList = "channel_id"),
  @Index(name = "idx_override_channel_holder", columnList = "channel_id, holder_id", unique = true)
})
public class ChannelPermissionOverride {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "channel_id", nullable = false)
  private Long channelId;

  @Column(name = "holder_id", nullable = false)
  private String holderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "permission_holder_type", nullable = false)
  private PermissionHolderType holderType;

  @Column(name = "allow_mask", nullable = false)
  private Long allowMask;

  @Column(name = "deny_mask", nullable = false)
  private Long denyMask;
}
