package com._labor.fakecord.infrastructure.presence;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface PresenceService {
  void enterChannel(UUID userId, Long channelId);
  void leaveChannel(UUID userId);
  public Optional<Long> getCurrentChannel(UUID userId);
  boolean isUserInChannel(UUID userId, Long channelId);
  Stream<UUID> streamChannelMembers(Long channelId);
  void addViewer(UUID targetId, UUID observerId);
  void removeViewer(UUID targetId, UUID observerId);
  Stream<UUID> streamActiveViewers(UUID targetId);
}
