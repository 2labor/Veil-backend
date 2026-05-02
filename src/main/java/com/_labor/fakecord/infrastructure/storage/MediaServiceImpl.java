package com._labor.fakecord.infrastructure.storage;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UploadResponse;
import com._labor.fakecord.domain.entity.UserProfile;
import com._labor.fakecord.domain.enums.ImageType;
import com._labor.fakecord.domain.enums.MediaType;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.UserProfileRepository;
import com._labor.fakecord.services.UserProfileCache;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

  private final FileStorageService fileStorageService;
  private final UserProfileRepository profileRepository;
  private final UserProfileCache cache;
  private final OutboxService outboxService;

  @Value("${app.s3.endpoint}")
    private String s3Endpoint;

  @Value("${app.s3.bucket-name}")
  private String bucketName;

  public MediaServiceImpl(
    FileStorageService fileStorageService,
    UserProfileRepository profileRepository,
    UserProfileCache cache,
    OutboxService outboxService
  ) {
    this.fileStorageService = fileStorageService;
    this.profileRepository = profileRepository;
    this.cache = cache;
    this.outboxService = outboxService;
  }

  @Override
  @Transactional
  public UploadResponse getAvatarUploadUrl(UUID userId, ImageType type) {
    return processUploadUrl(userId, type, MediaType.AVATAR);
  }

  @Override
  @Transactional
  public UploadResponse getBannerUploadUrl(UUID userId, ImageType type) {
    return processUploadUrl(userId, type, MediaType.BANNER);
  }

  private UploadResponse processUploadUrl(UUID userId, ImageType imageType, MediaType mediaType) {

    log.info("Processing {} upload for user: {}", mediaType, userId);

    UserProfile profile = profileRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("Profile not found"));

    String objectPath = mediaType.getFolder() + "/" + userId;

    String uploadUrl = fileStorageService.generateUploadUrl(objectPath, imageType.getMimeType());

    String version = String.valueOf(System.currentTimeMillis());
    String publicUrl = String.format("%s/%s/%s?v=%s", s3Endpoint, bucketName, objectPath, version);

    if (mediaType == MediaType.AVATAR) {
      profile.setAvatarUrl(publicUrl);
    } else if (mediaType == MediaType.BANNER) {
      profile.setBannerUrl(publicUrl);
    }

    return UploadResponse.builder()
      .uploadUrl(uploadUrl)
      .publicUrl(publicUrl)
      .build();
  }  
}
