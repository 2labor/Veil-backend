package com._labor.fakecord.services;

import com._labor.fakecord.domain.dto.AttachmentTask;

public interface MediaProcessingService {
  void processImageThumbnail(AttachmentTask task);
}
