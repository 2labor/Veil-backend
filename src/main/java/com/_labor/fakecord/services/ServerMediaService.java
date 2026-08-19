package com._labor.fakecord.services;

import java.util.UUID;

import com._labor.fakecord.domain.dto.PresignedUrlResponseDto;

public interface ServerMediaService {
  PresignedUrlResponseDto generateServerIconUploadUrl(UUID operatorId, Long serverId, String fileName, String contentType, Long fileSize);
  PresignedUrlResponseDto generateServerBannerUploadUrl(UUID operatorId, Long serverId, String fileName, String contentType, Long fileSize);
  void deleteServerIcon(UUID operatorId, Long serverId);
  void deleteServerBanner(UUID operatorId, Long serverId);
}
