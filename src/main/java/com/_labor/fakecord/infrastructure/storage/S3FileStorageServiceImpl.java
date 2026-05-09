package com._labor.fakecord.infrastructure.storage;

import java.io.InputStream;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
public class S3FileStorageServiceImpl implements FileStorageService {

  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  @Value("${app.s3.bucket-name}")
  private String bucketName;

  public S3FileStorageServiceImpl(S3Presigner s3Presigner, S3Client s3Client) {
    this.s3Presigner = s3Presigner;
    this.s3Client = s3Client;
  }

  @Override
  public String generateUploadUrl(String objectPath, String contentType) {
    log.debug("Generating presigned URL for path: {} [type: {}]", objectPath, contentType);

    PutObjectRequest objectRequest = PutObjectRequest.builder()
      .bucket(bucketName)
      .key(objectPath)
      .contentType(contentType)
      .build();
    
    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
      .signatureDuration(Duration.ofMinutes(15))
      .putObjectRequest(objectRequest)
      .build();

    return s3Presigner.presignPutObject(presignRequest).url().toString();
  }

  @Override
  public void delete(String objectPath) {
    try {
      log.info("Deleting object from S3: {}/{}", bucketName, objectPath);

      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(objectPath)
        .build();

      s3Client.deleteObject(deleteObjectRequest);

      log.debug("Object {} successfully deleted", objectPath);
    } catch (Exception e) {
      log.error("Failed to delete object {} from S3", objectPath, e);
    }
  }

  @Override
  public InputStream download(String objectPath) {
    log.debug("Downloading object from S3: {}", objectPath);
    try {
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
      .bucket(bucketName)
      .key(objectPath)
      .build();

      return s3Client.getObject(getObjectRequest);
    } catch (Exception e) {
      log.error("Failed to download object {} from S3", objectPath, e);
      throw new RuntimeException("S3 Download failed", e);
    }
  }

  @Override
  public void update(InputStream inputStream, String objectPath, String contentType, long contentLength) {
    log.debug("Uploading object to S3: {} [size: {}]", objectPath, contentLength);
    try {
      PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(objectPath)
        .contentType(contentType)
        .build();
        
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, contentLength));
      
        log.debug("Successfully uploaded {} to S3", objectPath);
    } catch (Exception e) {
      log.error("Failed to upload object {} to S3", objectPath, e);
      throw new RuntimeException("S3 Upload failed", e);
    }
  }
  
}
