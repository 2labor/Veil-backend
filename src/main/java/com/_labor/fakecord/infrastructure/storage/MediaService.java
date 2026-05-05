package com._labor.fakecord.infrastructure.storage;

import java.util.UUID;

import org.apache.tomcat.util.http.parser.MediaType;

import com._labor.fakecord.domain.dto.UploadResponse;
import com._labor.fakecord.domain.enums.ImageType;

public interface MediaService {
  UploadResponse getAvatarUploadUrl(UUID userId, ImageType type);
  UploadResponse getBannerUploadUrl(UUID userId, ImageType type);
  UploadResponse prepareAttachment(UUID userId, String originalFileName, String contentType, long fileSize);
}
