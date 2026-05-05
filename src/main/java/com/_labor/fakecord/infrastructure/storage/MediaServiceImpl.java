package com._labor.fakecord.infrastructure.storage;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UploadResponse;
import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.entity.UserProfile;
import com._labor.fakecord.domain.enums.AttachmentStatus;
import com._labor.fakecord.domain.enums.AttachmentType;
import com._labor.fakecord.domain.enums.ImageType;
import com._labor.fakecord.domain.enums.MediaType;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.AttachmentRepository;
import com._labor.fakecord.repository.UserProfileRepository;
import com._labor.fakecord.services.UserProfileCache;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MediaServiceImpl implements MediaService {

  private final SecurityFilterChain filterChain;
  private final FileStorageService fileStorageService;
  private final UserProfileRepository profileRepository;
  private final AttachmentRepository attachmentRepository;
  private final UserProfileCache cache;
  private final OutboxService outboxService;

  @Value("${app.s3.endpoint}")
    private String s3Endpoint;

  @Value("${app.s3.bucket-name}")
  private String bucketName;

  public MediaServiceImpl(
    FileStorageService fileStorageService,
    UserProfileRepository profileRepository,
    AttachmentRepository attachmentRepository,
    UserProfileCache cache,
    OutboxService outboxService, 
    SecurityFilterChain filterChain
  ) {
    this.fileStorageService = fileStorageService;
    this.profileRepository = profileRepository;
    this.attachmentRepository = attachmentRepository;
    this.cache = cache;
    this.outboxService = outboxService;
    this.filterChain = filterChain;
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

  @Override
  public UploadResponse prepareAttachment(UUID userId, String originalFileName, String contentType, long fileSize) {
    log.info("Preparing attachment upload for user: {} [file: {}]", userId, originalFileName);

    UUID attachmentId = UUID.randomUUID();

    AttachmentType type = determineAttachmentType(contentType);

    String extension = getFileExtension(originalFileName);
    String storagePath = String.format("attachments/%s/%s%s", userId, attachmentId, extension);

    Attachment attachment = Attachment.builder()
      .id(attachmentId)
      .ownerId(userId)
      .fileName(originalFileName)
      .storageName(storagePath)
      .contentType(contentType)
      .fileSize(fileSize)
      .attachmentType(type)
      .attachmentStatus(AttachmentStatus.PENDING)
      .build();

    attachmentRepository.save(attachment);

    String uploadUrl = fileStorageService.generateUploadUrl(storagePath, contentType);
    String publicUrl = String.format("%s/%s/%s", s3Endpoint, bucketName, storagePath);

    return UploadResponse.builder()
      .id(attachmentId)
      .uploadUrl(uploadUrl)
      .publicUrl(publicUrl)
      .build();
  }  

  private AttachmentType determineAttachmentType(String contentType) {
    if (contentType == null) return AttachmentType.DOCUMENT;
    if (contentType.startsWith("/image")) return AttachmentType.IMAGE;
    if (contentType.startsWith("/video")) return AttachmentType.VIDEO;
    return AttachmentType.DOCUMENT;
  }

  private String getFileExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) return "";
    return fileName.substring(fileName.lastIndexOf("."));
  }
}
