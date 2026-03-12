package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.FriendRequest;
import com._labor.fakecord.domain.enums.RelationshipStatus;
import com._labor.fakecord.domain.enums.RequestSource;
import com._labor.fakecord.domain.enums.RequestStatus;
import com._labor.fakecord.infrastructure.outbox.domain.FriendAcceptedPayload;
import com._labor.fakecord.infrastructure.outbox.domain.FriendRequestPayload;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.RelationshipActionPayload;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.FriendRequestRepository;
import com._labor.fakecord.repository.UserRepository;
import com._labor.fakecord.services.FriendRequestCommandService;
import com._labor.fakecord.services.FriendRequestQueryService;
import com._labor.fakecord.services.RelationshipCommandService;
import com._labor.fakecord.services.RelationshipQueryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestCommandService, FriendRequestQueryService {

  private final FriendRequestRepository repository;
  private final RelationshipCommandService relationshipCommandService;
  private final RelationshipQueryService relationshipQueryService;
  private final UserRepository userRepository;
  private final OutboxService outboxService;

  @Override
  @Transactional
  public void sendFriendRequest(UUID senderId, UUID targetId, RequestSource source) {
    if (senderId.equals(targetId)) {
      throw new RuntimeException("You cannot send request to yourself");
    }

    RelationshipStatus status = relationshipQueryService.getRelationshipStatus(senderId, targetId);

    if (status == RelationshipStatus.FRIENDS) {
      throw  new RuntimeException("Already friends");
    }

    if (relationshipQueryService.isBlocked(targetId, senderId)) {
      throw new RuntimeException("You cannot send request to user who blocked you");
    }

    repository.findBySenderIdAndTargetId(senderId, targetId)
      .ifPresent(r -> {
        throw new RuntimeException("Request already pending");
    });

   repository.findBySenderIdAndTargetId(targetId, senderId).ifPresentOrElse(existingRequest -> {
        relationshipCommandService.createFriendship(senderId, targetId);
        repository.delete(existingRequest);
        log.info("Mutual request detected, friendship created between {} and {}", senderId, targetId);
    }, () -> {
        FriendRequest request = new FriendRequest();
        request.setSender(userRepository.getReferenceById(senderId));
        request.setTarget(userRepository.getReferenceById(targetId));
        request.setSource(source);
        request.setStatus(RequestStatus.PENDING);
        repository.save(request);
        
        outboxService.publish(senderId, OutboxEventType.SOCIAL_FRIEND_REQUEST_SENT, new RelationshipActionPayload(senderId, targetId));
    });
    log.info("Friend request sent from {} to {}", senderId, targetId);
  }

  @Override
  @Transactional
  public void acceptFriendRequest(UUID currentUser, UUID requesterId) {
    FriendRequest request = repository.findBySenderIdAndTargetId(requesterId, currentUser)
    .filter(r -> r.getStatus() == RequestStatus.PENDING)
      .orElseThrow(() -> new RuntimeException("Request not found"));

    relationshipCommandService.createFriendship(requesterId, currentUser);

    repository.delete(request);

    log.info("User {} accepted friend request from {}", currentUser, requesterId);
  }

  @Override
  @Transactional
  public void declineOrCancelRequest(UUID senderId, UUID targetId) {
    repository.findBySenderIdAndTargetId(senderId, targetId)
      .ifPresent(r -> {
        OutboxEventType type = r.getSender().getId().equals(targetId) 
          ? OutboxEventType.SOCIAL_FRIEND_REQUEST_DECLINED
          : OutboxEventType.SOCIAL_FRIEND_REQUEST_CANCELLED;

        outboxService.publish(
          senderId, 
          type, 
          new RelationshipActionPayload(senderId, targetId)
        );

        repository.delete(r);
      });
    log.info("Request between {} and {} was cancelled/declined", senderId, targetId);
  }

  @Override
  @Transactional
  public void ignoreRequest(UUID currentUserId, UUID requesterId) {
    repository.findBySenderIdAndTargetId(requesterId, currentUserId).ifPresent(r -> {
      r.setStatus(RequestStatus.IGNORED);
      repository.save(r);
    });

    outboxService.publish(
      currentUserId, 
      OutboxEventType.SOCIAL_FRIEND_REQUEST_IGNORED,
      new RelationshipActionPayload(currentUserId, requesterId));
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<UserProfileShort> getIncomingRequests(UUID userId, Pageable pageable) {
    return repository.findAllIncomingShort(userId, RequestStatus.PENDING, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public Slice<UserProfileShort> getOutgoingRequests(UUID userId, Pageable pageable) {
    return repository.findAllOutgoingShort(userId, RequestStatus.PENDING, pageable);
  }

  @Override
  @Transactional(readOnly = true)
  public long getCounterIncomingRequests(UUID userId) {
    return repository.countIncomingRequests(userId);
  }

  @Override
  public long getCounterOutgoingRequests(UUID userId) {
    return repository.countOutgoingRequests(userId);
  }
}
