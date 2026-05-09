package com._labor.fakecord.infrastructure.outbox.service.impl;

import org.springframework.stereotype.Service;

import org.springframework.kafka.annotation.KafkaListener;
import com._labor.fakecord.infrastructure.outbox.domain.payload.MediaTaskPayload;
import com._labor.fakecord.services.MediaProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailWorker {
  
  private final ObjectMapper objectMapper;
  private final MediaProcessingService mediaProcessService;

  @KafkaListener(
    topics = "${app.kafka.topics.media-processing:media.processing}",
    groupId = "fakecord-thumbnail-group"
)
  public void handleMediaTask(String eventPayload) {
    log.info("Media task received from Kafka");
    try {
      String finalJson = eventPayload;
      if (eventPayload.startsWith("\"") && eventPayload.endsWith("\"")) {
        finalJson = objectMapper.readValue(eventPayload, String.class);
      }

      MediaTaskPayload payload = objectMapper.readValue(finalJson, MediaTaskPayload.class);

      log.debug("Processing {} attachments for owner {}", payload.items().size(), payload.ownerId());
      
      payload.items().forEach(item -> {
        try {
          mediaProcessService.processImageThumbnail(item);
        } catch (Exception e) {
          log.error("Failed to process attachment {}: {}", item.attachmentId(), e.getMessage());
        }
      });
  } catch (Exception e) {
      log.error("Critical error while parsing MediaTaskPayload: {}", eventPayload, e);
  }
  }
}
