package com._labor.fakecord.services;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com._labor.fakecord.domain.dto.MessageReactionsResponse;
import com._labor.fakecord.domain.events.ReactionEvent;

public interface ReactionService {
  ReactionEvent toggleReaction(Long messageId, Long emojiId, UUID userId);
  void clearAllReactions(Long messageId, UUID operatorId);
  List<MessageReactionsResponse> getAllReactionsForMessage(Long messageId, UUID currentUserId);
  Map<Long, List<MessageReactionsResponse>> getReactionsToMessages(List<Long> messageIds, UUID currentUserId);
  // Slice<UserProfileShort> getUsersForReactionSlice(Long messageId, Long emojiId, Pageable pageable);
}