package com._labor.fakecord.domain.enums;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum MediaContentType {
  IMAGE_JPEG("image/jpeg", ".jpg"),
  IMAGE_PNG("image/png", ".png"),
  IMAGE_GIF("image/gif", ".gif"),
  IMAGE_WEBP("image/webp", ".webp");

  private final String mimeType;
  private final String extension;

  MediaContentType(String mimeType, String extension) {
    this.mimeType = mimeType;
    this.extension = extension;
  }

  public static MediaContentType fromMimeType(String mimeType) {
    return Arrays.stream(values())
      .filter(type -> type.getMimeType().equalsIgnoreCase(mimeType))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Unsupported content type: " + mimeType));
  }
}
