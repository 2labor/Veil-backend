package com._labor.fakecord.infrastructure.outbox.service.handler;

import java.util.Set;

import org.springframework.stereotype.Component;

import com._labor.fakecord.infrastructure.cache.services.PermissionCache;
import com._labor.fakecord.infrastructure.cache.services.ServerCache;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEvent;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.payload.ServerDeletedPayload;
import com._labor.fakecord.infrastructure.outbox.domain.payload.ServerMemberEventPayload;
import com._labor.fakecord.infrastructure.outbox.domain.payload.ServerOwnershipTransferredPayload;
import com._labor.fakecord.infrastructure.outbox.service.OutboxHandler;
import com._labor.fakecord.services.ServerDomainService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServerOutboxHandler implements OutboxHandler {

  private final ObjectMapper objectMapper;
  private final ServerDomainService serverDomainService;
  private final ServerCache serverCache;
  private final PermissionCache permissionCache;

  private static final Set<OutboxEventType> SUPPORTED = Set.of(
    OutboxEventType.SERVER_MEMBER_JOINED,
    OutboxEventType.SERVER_MEMBER_LEFT,
    OutboxEventType.SERVER_OWNERSHIP_TRANSFERRED,
    OutboxEventType.SERVER_DELETED
  );

  @Override
  public boolean supports(OutboxEventType type) {
    return SUPPORTED.contains(type);
  }

  @Override
  public void handle(OutboxEvent event) {
    try {
      switch (event.getType()) {
        case SERVER_MEMBER_JOINED -> {
          ServerMemberEventPayload payload = objectMapper.readValue(event.getPayload(), ServerMemberEventPayload.class);
          serverDomainService.incrementMemberCounter(payload.serverId());
          log.info("Handled SERVER_MEMBER_JOINED for server {} and user {}", payload.serverId(), payload.userId());
        } 

        case SERVER_MEMBER_LEFT -> {
          ServerMemberEventPayload payload = objectMapper.readValue(event.getPayload(), ServerMemberEventPayload.class);
          serverDomainService.decrementMemberCounter(payload.serverId());
          permissionCache.evictUserServerPermission(payload.serverId(), payload.userId());
          log.info("Handled SERVER_MEMBER_LEFT for server {} and user {}", payload.serverId(), payload.userId());
        }

        case SERVER_OWNERSHIP_TRANSFERRED -> {
          ServerOwnershipTransferredPayload payload = objectMapper.readValue(event.getPayload(), ServerOwnershipTransferredPayload.class);
          serverCache.evict(payload.serverId());
          permissionCache.evictServerPermissionAll(payload.serverId());
          permissionCache.evictChannelPermissionsAll(payload.serverId());
          log.info("Handled SERVER_OWNERSHIP_TRANSFERRED for server {}: {} -> {}", payload.serverId(), payload.oldOwnerId(), payload.newOwnerId());
        }

        case SERVER_DELETED -> {
          ServerDeletedPayload payload = objectMapper.readValue(event.getPayload(), ServerDeletedPayload.class);

          serverCache.evict(payload.serverId());
          permissionCache.evictServerPermissionAll(payload.serverId());
          permissionCache.evictChannelPermissionsAll(payload.serverId());
          log.info("Handled SERVER_DELETED for server {}", payload.serverId());
        }
      }
      log.info("Server outbox event {} processed successfully", event.getType());
    } catch (Exception e) {
      log.error("Failed to process server outbox event {}: {}", event.getId(), e.getMessage(), e);
      throw new RuntimeException("Server outbox processing failed", e);
    }
  }

}