package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Relationships;
import com._labor.fakecord.domain.enums.RelationshipStatus;
import com._labor.fakecord.repository.FriendRequestRepository;
import com._labor.fakecord.repository.RelationshipRepository;
import com._labor.fakecord.repository.UserRepository;
import com._labor.fakecord.services.RelationshipService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RelationshipServiceImpl implements RelationshipService{

  private final RelationshipRepository repository;
  private final UserRepository userRepository;
  private final FriendRequestRepository friendRequestRepository;

  @Override
  @Transactional
  public void removeFriend(UUID userId, UUID friendId) {
    log.info("Removing friendship between {} and {}", userId, friendId);
    repository.deleteByUserIdAndTargetIdOrUserIdAndTargetId(userId, friendId, friendId, userId);
  }

  @Override
  @Transactional
  public void blockUser(UUID senderId, UUID targetId) {
    if (senderId.equals(targetId)) {
      throw new IllegalArgumentException("You cannot block yourself");
    }

    repository.deleteByUserIdAndTargetIdOrUserIdAndTargetId(senderId, targetId, targetId, senderId);

    friendRequestRepository.findBySenderIdAndTargetId(senderId, targetId).ifPresent(friendRequestRepository::delete);
    friendRequestRepository.findBySenderIdAndTargetId(targetId, senderId).ifPresent(friendRequestRepository::delete);

    Relationships block = repository.findByUserIdAndTargetId(senderId, targetId)
      .orElse(new Relationships());

    block.setUser(userRepository.getReferenceById(senderId));
    block.setTarget(userRepository.getReferenceById(targetId));
    block.setStatus(RelationshipStatus.BLOCKED);

    repository.save(block);
    log.info("User {} blocked {}", senderId, targetId);
  }

  @Override
  @Transactional
  public void unblockUser(UUID senderId, UUID targetId) {
    repository.findByUserIdAndTargetId(senderId, targetId)
      .filter(r -> r.getStatus() == RelationshipStatus.BLOCKED)
      .ifPresent(repository::delete);
    log.info("User {} unblocked {}", senderId, targetId);
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<UserProfileShort> getFriendsList(UUID userId, Pageable pageable) {
    return repository.findAllFriendsShort(userId, RelationshipStatus.FRIENDS, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<UserProfileShort> getBlockedUsers(UUID userId, Pageable pageable) {
    return repository.findAllFriendsShort(userId, RelationshipStatus.BLOCKED, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public RelationshipStatus getRelationshipStatus(UUID userA, UUID userB) {
    return repository.findByUserIdAndTargetId(userA, userB)
      .map(Relationships::getStatus)
      .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserProfileShort> getMutualFriends(UUID userA, UUID userB) {
    return repository.findMutualFriends(userA, userB);
  }

  @Override
  @Transactional(readOnly = true)
  public long getMutualFriendsCount(UUID userA, UUID userB) {
    return repository.countMutualFriends(userA, userB);
  }
  
}
