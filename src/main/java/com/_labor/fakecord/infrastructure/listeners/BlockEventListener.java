package com._labor.fakecord.infrastructure.listeners;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.events.UserBlockedEvent;
import com._labor.fakecord.repository.FriendRequestRepository;
import com._labor.fakecord.services.FriendRequestCommandService;
import com._labor.fakecord.services.RelationshipCommandService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BlockEventListener {

  private final FriendRequestCommandService friendService;
  private final RelationshipCommandService relationshipService;
  private final FriendRequestRepository friendRequestRepository;

  @EventListener
  @Transactional
  public void handleBlockUser(UserBlockedEvent event) {
    UUID targetId = event.targetId();
    UUID actorId = event.actorId();


    friendRequestRepository.deleteBetweenUsers(actorId, targetId);
    relationshipService.forceTerminateRelationships(actorId, targetId);
  }

}