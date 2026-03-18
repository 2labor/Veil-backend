package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.User;
import com._labor.fakecord.domain.entity.UserBlock;
import com._labor.fakecord.domain.events.UserBlockedEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.RelationshipActionPayload;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.UserBlockRepository;
import com._labor.fakecord.repository.UserRepository;
import com._labor.fakecord.services.UserBlockService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBlockServiceImpl implements  UserBlockService{

  private final UserBlockRepository repository;
  private final OutboxService outboxService;
  private final ApplicationEventPublisher eventPublisher;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public void blockUser(UUID actorId, UUID targetId) {
    log.info("Attempting to block user {} by actor {}", targetId, actorId);

    if (actorId.equals(targetId)) {
      log.warn("Security attempt: User {} tried to block themselves", actorId);
      throw new IllegalArgumentException("Cannot block yourself");
    }

    if (repository.existsByUserIdAndTargetId(actorId, targetId)) {
      log.info("User {} is already blocked by {}", targetId, actorId);
      return;
    }

    try {
      eventPublisher.publishEvent(new UserBlockedEvent(actorId, targetId));

      UserBlock block = new UserBlock();
      block.setUser(new User(actorId));
      block.setTarget(new User(targetId));
      repository.save(block);

      outboxService.publish(actorId, OutboxEventType.SOCIAL_USER_BLOCKED, 
        new RelationshipActionPayload(actorId, targetId));
    
      log.info("Successfully blocked user {} by {}", targetId, actorId);
    } catch (Exception e) {
      log.error("Failed to block user {} by {}. Error: {}", targetId, actorId, e.getMessage());
      throw e;
    }
  }

  @Override
  @Transactional
  public void unblockUser(UUID actorId, UUID targetId) {
    log.info("Unblocking user {} by actor {}", targetId, actorId);

    repository.deleteByUserIdAndTargetId(actorId, targetId);

    outboxService.publish(actorId, OutboxEventType.SOCIAL_USER_UNBLOCKED,
      new RelationshipActionPayload(actorId, targetId));
      
    log.info("Successfully unblocked user {} by {}", targetId, actorId);
  }

  @Override
  public boolean isBlocked(UUID actorId, UUID targetId) {
    return repository.existsByUserIdAndTargetId(actorId, targetId);
  }

  @Override
  public Slice<UUID> getBlockedIds(UUID actorId, Pageable pageable) {
    return repository.findBlockedIdsByUserId(actorId, pageable);
  }

  @Override
  public boolean isBlockedBy(UUID targetId, UUID actorId) {
    return repository.existsByUserIdAndTargetId(targetId, actorId);
  }
  
  @Override
  public Slice<UserProfileShort> getBlockedUsersList(UUID userId, Pageable pageable) {
    return repository.findAllBlockedProfiles(userId, pageable);
  }
  
}
