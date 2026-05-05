package com._labor.fakecord.domain.mappper.Impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.AttachmentDto;
import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.mappper.AttachmentMapper;


@Component
public class AttachmentMapperImpl implements AttachmentMapper{

  @Value("${app.s3.endpoint}")
  private String s3Endpoint;

  @Value("${app.s3.bucket-name}")
  private String bucketName;

  @Override
  public AttachmentDto toDto(Attachment entity) {
    if (entity == null) return null;

    return AttachmentDto.builder()
      .id(entity.getId())
      .fileName(entity.getFileName()) 
      .url(buildUrl(entity.getStorageName()))
      .thumbnailUrl(entity.getThumbnailUrl() != null ? buildUrl(entity.getThumbnailUrl()) : null)
      .type(entity.getAttachmentType())
      .fileSize(entity.getFileSize())
      .metadata(entity.getMetadata())
      .build();
  }

  @Override
  public List<AttachmentDto> toListDto(List<Attachment> entities) {
    if (entities == null || entities.isEmpty()) return List.of();

    return entities.stream()
      .map(this::toDto)
      .collect(Collectors.toList());
  }
 
  private String buildUrl(String storagePath) {
    if (storagePath == null) return null;
    return String.format("%s/%s/%s", s3Endpoint, bucketName, storagePath);
  }
}
