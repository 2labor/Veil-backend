package com._labor.fakecord.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com._labor.fakecord.domain.entity.UserProfile;

import io.lettuce.core.dynamic.annotation.Param;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>{
  @Query("SELECT up FROM UserProfile up WHERE up.handle = :handle AND up.discriminator = :discriminator")
  Optional<UserProfile> findByFullHandle(
    @Param("handle") String handle, 
    @Param("discriminator") String discriminator
  );
  List<UserProfile> findAllByUserIdIn(Collection<UUID> userIds);
  boolean existsByHandleAndDiscriminator(String handle, String tag);
}