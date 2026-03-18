package com._labor.fakecord.domain.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import com._labor.fakecord.domain.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_profiles", uniqueConstraints = {
  @UniqueConstraint(name = "uk_handle_discriminator", columnNames = {"handle", "discriminator"})
})
public class UserProfile {
  @Id
  @Column(name = "user_id")
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "user_name", nullable = false, length = 32)
  private String displayName;

  @Column(name = "handle", nullable = false, length = 24)
  private String handle;

  @Column(name = "is_handle_set", nullable = false)
  private boolean isHandleSet = false;

  @Column(name = "discriminator", nullable = false, length = 4)
  private String discriminator;

  @Column(name = "global_id", nullable = false, updatable = false, insertable = false, unique = true, columnDefinition = "bigserial")
  private Long globalId;

  @Column(name = "avatar_url", length = 2048)
  private String avatarUrl;

  @Column(name = "banner_url", length = 2048)
  private String bannerUrl;

  @Column(name = "bio", length = 254)
  private String bio;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private UserStatus statusPreference = UserStatus.ONLINE;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private ProfileMetadata metadata = new ProfileMetadata();

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  public UserProfile(User user, String displayName) {
    this.user = user;
    this.displayName = displayName;
  }
}
