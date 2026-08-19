package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.UploadResponse;
import com._labor.fakecord.domain.enums.ImageType;
import com._labor.fakecord.infrastructure.storage.MediaService;

@RestController
@RequestMapping("/api/media")
public class MediaController {
  
  private final MediaService mediaService;

  public MediaController(MediaService mediaService) { 
    this.mediaService = mediaService;
  }


  @PostMapping("/avatar")
  public ResponseEntity<UploadResponse> getAvatarUrl(
    Principal principal,
    @RequestParam ImageType type,
    @RequestParam Long fileSize
  ) {
    UUID userId = UUID.fromString(principal.getName()); 
    return ResponseEntity.ok(mediaService.getAvatarUploadUrl(userId, type, fileSize));
  }

  @PostMapping("/banner")
  public ResponseEntity<UploadResponse> getBanner(
    Principal principal,
    @RequestParam ImageType type,
    @RequestParam Long fileSize
  ) {
    UUID userId = UUID.fromString(principal.getName()); 
    return ResponseEntity.ok(mediaService.getBannerUploadUrl(userId, type, fileSize));
  }
}
