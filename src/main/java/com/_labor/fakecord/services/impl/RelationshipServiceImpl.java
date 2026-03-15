package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Relationships;
import com._labor.fakecord.domain.enums.RelationshipStatus;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.RelationshipActionPayload;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.FriendRequestRepository;
import com._labor.fakecord.repository.RelationshipRepository;
import com._labor.fakecord.repository.UserRepository;
import com._labor.fakecord.services.RelationshipCommandService;
import com._labor.fakecord.services.RelationshipQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RelationshipServiceImpl implements RelationshipCommandService, RelationshipQueryService {

  private final RelationshipRepository repository;
  private final UserRepository userRepository;
  private final FriendRequestRepository friendRequestRepository;
  private final OutboxService outboxService;

  @Override
  @Transactional
  public void createFriendship(UUID userA, UUID userB) {
    if (userA.equals(userB)) {
      throw new RuntimeException("You cannot add yourself in friends");
    }

    repository.findByUserIdAndTargetId(userA, userB).ifPresent(r -> {
      throw new RuntimeException("Already friends!");
    });

    Relationships r1 = new Relationships();
    r1.setUser(userRepository.getReferenceById(userA));
    r1.setTarget(userRepository.getReferenceById(userB));
    r1.setStatus(RelationshipStatus.FRIENDS);

    Relationships r2 = new Relationships();
    r2.setUser(userRepository.getReferenceById(userB));
    r2.setTarget(userRepository.getReferenceById(userA));
    r2.setStatus(RelationshipStatus.FRIENDS);
  
    repository.saveAll(List.of(r1, r2));

    outboxService.publish(
        userA, 
        OutboxEventType.SOCIAL_FRIENDSHIP_CREATED, 
        new RelationshipActionPayload(userA, userB)
    );

    log.info("Friendship established between {} and {}", userA, userB);
  }

  @Override
  @Transactional
  public void removeFriend(UUID userId, UUID friendId) {
    log.info("Removing friendship between {} and {}", userId, friendId);
    repository.deleteByUserIdAndTargetIdOrUserIdAndTargetId(userId, friendId, friendId, userId);

    outboxService.publish(
      userId, 
      OutboxEventType.SOCIAL_FRIENDSHIP_TERMINATED, 
      new RelationshipActionPayload(userId, friendId)
    );
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

    outboxService.publish(
      senderId, 
      OutboxEventType.SOCIAL_USER_BLOCKED, 
      new RelationshipActionPayload(senderId, targetId)
    );
    log.info("User {} blocked {}", senderId, targetId);
  }

  @Override
  @Transactional
  public void unblockUser(UUID senderId, UUID targetId) {
    repository.findByUserIdAndTargetId(senderId, targetId)
      .filter(r -> r.getStatus() == RelationshipStatus.BLOCKED)
      .ifPresent(repository::delete);

    outboxService.publish(
      senderId, 
      OutboxEventType.SOCIAL_USER_UNBLOCKED,
      new RelationshipActionPayload(senderId, targetId)
    );

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

  @Override
  public boolean isBlocked(UUID senderId, UUID targetId) {
    return repository.findByUserIdAndTargetId(senderId, targetId)
      .map(r -> r.getStatus() == RelationshipStatus.BLOCKED)
      .orElse(false);
  }
}
