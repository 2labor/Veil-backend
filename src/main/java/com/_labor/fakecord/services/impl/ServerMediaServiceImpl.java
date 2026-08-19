package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.PresignedUrlResponseDto;
import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.infrastructure.storage.FileStorageService;
import com._labor.fakecord.repository.ServerRepository;
import com._labor.fakecord.services.ServerMediaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServerMediaServiceImpl implements ServerMediaService {

  private static final long MAX_ICON_SIZE_BYTES = 5 * 1024 * 1024;
  private static final long MAX_BANNER_SIZE_BYTES = 10 * 1024 * 1024;

  @Value("${app.s3.bucket-name}")
  private String bucketName;

  @Value("${app.s3.endpoint}")
  private String s3Endpoint;

  private final FileStorageService storageService;
  private final ServerRepository serverRepository;

  @Override
  public PresignedUrlResponseDto generateServerIconUploadUrl(UUID operatorId, Long serverId, String fileName, String contentType, Long fileSize) {
    validateFileSize(fileSize, MAX_ICON_SIZE_BYTES);

    String extension = extractExtension(fileName);
    String objectPath = String.format("server/%d/icon/%s.%s", serverId, UUID.randomUUID(), extension);

    String uploadUrl = storageService.generateUploadUrl(objectPath, contentType, fileSize);
    String publicUrl = String.format("%s/%s/%s", s3Endpoint, bucketName, objectPath);

    return new PresignedUrlResponseDto(uploadUrl, objectPath, publicUrl);
  }

  @Override
  public PresignedUrlResponseDto generateServerBannerUploadUrl(UUID operatorId, Long serverId, String fileName, String contentType, Long fileSize) {
    validateFileSize(fileSize, MAX_BANNER_SIZE_BYTES);

    String extension = extractExtension(fileName);
    String objectPath = String.format("server/%d/banner/%s.%s", serverId, UUID.randomUUID(), extension);

    String uploadUrl = storageService.generateUploadUrl(objectPath, contentType, fileSize);
    String publicUrl = String.format("%s/%s/%s", s3Endpoint, bucketName, objectPath);

    return new PresignedUrlResponseDto(uploadUrl, objectPath, publicUrl);
  }

  @Override
  @Transactional
  public void deleteServerIcon(UUID operatorId, Long serverId) {
    Server server = serverRepository.findById(serverId)
      .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + serverId));

    if (server.getIconUrl() != null) {
      storageService.delete(server.getIconUrl());
      server.setIconUrl(null);
      serverRepository.save(server);
      log.info("Server icon for server {} successfully deleted by operator {}", serverId, operatorId);
    }
  }

  @Override
  @Transactional
  public void deleteServerBanner(UUID operatorId, Long serverId) {
    Server server = serverRepository.findById(serverId)
      .orElseThrow(() -> new IllegalArgumentException("Server not found with id: " + serverId));

    if (server.getBannerUrl() != null) {
      storageService.delete(server.getBannerUrl());
      server.setBannerUrl(null);
      serverRepository.save(server);
      log.info("Server banner for server {} successfully deleted by operator {}", serverId, operatorId);
    }
  }

  private void validateFileSize(Long fileSize, Long maxSize) {
    if (fileSize == null || fileSize <= 0) {
      throw new IllegalArgumentException("File size must be greater than 0");
    }
    if (fileSize > maxSize) {
      throw new IllegalArgumentException(
        String.format("size exceeds the maximum limit of %d MB", maxSize / (1024 * 1024))
      );
    }
  }
  
  private String extractExtension(String fileName) {
    if (fileName == null || !fileName.contains(".")) return "png";

    return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
  }
}