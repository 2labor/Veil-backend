package com._labor.fakecord.infrastructure.storage;

import java.io.InputStream;

public interface FileStorageService {
  String generateUploadUrl(String objectPath, String contentType);
  void delete(String objectPath);
  InputStream download(String objectPath);
  void update(InputStream inputStream, String objectPath, String contentType, long contentLength);
}
