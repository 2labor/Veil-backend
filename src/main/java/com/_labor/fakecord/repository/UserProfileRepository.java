package com._labor.fakecord.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>{
  List<UserProfile> findAllByUserIdIn(Collection<UUID> userIds);
  boolean existsByHandleAndDiscriminator(String handle, String tag);
}