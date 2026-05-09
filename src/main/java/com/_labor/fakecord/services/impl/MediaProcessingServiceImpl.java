package com._labor.fakecord.services.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.AttachmentTask;
import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.enums.AttachmentStatus;
import com._labor.fakecord.infrastructure.storage.FileStorageService;
import com._labor.fakecord.repository.AttachmentRepository;
import com._labor.fakecord.services.MediaProcessingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaProcessingServiceImpl implements MediaProcessingService {

  private final FileStorageService storageService;
  private final AttachmentRepository attachmentRepository;

  private final static int THUMB_WIDTH = 320;
  private final static int THUMB_HEIGHT = 320;
  private final static float JPG_QUALITY = 0.7f;

  @Override
  public void processImageThumbnail(AttachmentTask task) {
    log.info("Processing thumbnail for attachment ID: {}", task.attachmentId());

    try (InputStream originalStream = storageService.download(task.storageName())) {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      Thumbnails.of(originalStream)
        .size(THUMB_WIDTH, THUMB_HEIGHT)
        .outputFormat("jpg")
        .outputQuality(JPG_QUALITY)
        .toOutputStream(outputStream);

        byte[] thumbData = outputStream.toByteArray();
        String thumbPath = task.storageName().replace("attachments/", "thumbnails/")
        .replaceAll("\\.[^.]+$", ".jpg");

        storageService.update(new ByteArrayInputStream(thumbData), thumbPath, "image/jpeg", (long) thumbData.length);
        
        updateAttachmentInDP(task.attachmentId(), thumbPath);
        log.info("Thumbnail successfully created: {}", thumbPath);
    } catch (Exception e) {
      log.error("Failed to generate thumbnail for attachment {}", task.attachmentId(), e);
      throw new RuntimeException("Media processing error", e);
    }
  }

  private void updateAttachmentInDP(UUID AttachmentId, String newThumbPath) {
    Attachment attachment = attachmentRepository.findById(AttachmentId)
      .orElseThrow(() -> new RuntimeException("Attachment record lost in DB during processing!"));

    attachment.setThumbnailUrl(newThumbPath);
    attachment.setAttachmentStatus(AttachmentStatus.ACTIVE);
    attachmentRepository.save(attachment);
  }
  
}
