package com._labor.fakecord.infrastructure.outbox.service.impl;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.entity.MessageReaction;
import com._labor.fakecord.domain.events.ReactionEvent;
import com._labor.fakecord.repository.MessageReactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReactionDatabaseWorker {
  
  private final ObjectMapper objectMapper;
  private final MessageReactionRepository repository;

  @Transactional
  @KafkaListener(topics = "chat.reactions", groupId = "fakecord-reaction-persister-group")
  public void handleReactionEvent(String rawPayload) {
    log.info("Reaction Worker: Processing event from Kafka...");
    try {
        String finalJson = rawPayload;
        if (rawPayload.startsWith("\"") && rawPayload.endsWith("\"")) {
          finalJson = objectMapper.readValue(rawPayload, String.class);
        }

        ReactionEvent event = objectMapper.readValue(finalJson, ReactionEvent.class);

        switch (event.action()) {
          case ADD -> {
            MessageReaction entity = new MessageReaction();
              entity.setId(event.id());
              entity.setMessageId(event.messageId());
              entity.setUserId(event.userId());
              entity.setEmojiId(event.emojiId());
              entity.setCreatedAt(Instant.now());

              if (!repository.existsById(event.id())) {
                repository.save(entity);
                log.debug("Reaction Worker: Saved reaction {} to DB for message {}", event.id(), event.messageId());
              }
          }

          case REMOVE -> {
            repository.deleteByMessageIdAndUserIdAndEmojiId(
              event.messageId(), 
              event.userId(), 
              event.emojiId()
            );
            log.debug("Reaction Worker: Removed reaction from DB for message {} by user {}", event.messageId(), event.userId());
        }
          case CLEAR_ALL -> {
            repository.deleteByMessageId(event.messageId());
            log.info("Reaction Worker: Database cleared for message {}", event.messageId());
          }
        }
    } catch (Exception e) {
      log.error("Critical error in ReactionDatabaseWorker while processing payload: {}", rawPayload, e);
      throw new RuntimeException("Failed to persist reaction event", e);
    }
  }
}
