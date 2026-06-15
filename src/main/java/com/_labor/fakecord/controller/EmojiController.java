package com._labor.fakecord.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.EmojiResponse;
import com._labor.fakecord.domain.enums.EmojiCategory;
import com._labor.fakecord.services.EmojiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/emoji")
@RequiredArgsConstructor
public class EmojiController {
  
  private final EmojiService service;

  @GetMapping("/categories/{category}")
  public ResponseEntity<List<EmojiResponse>> getGlobalEmojisByCategory(
    @PathVariable EmojiCategory category
  ) {
    List<EmojiResponse> emojis = service.getGlobalEmojisByCategory(category);
    return ResponseEntity.ok(emojis);
  }

  @GetMapping("/categories/{category}/{name}")
  public ResponseEntity<EmojiResponse> getGlobalEmojiByCategoryAndName(
    @PathVariable EmojiCategory category,
    @PathVariable String name
  ) {
    return service.getGlobalEmojiByCategoryAndName(category, name.trim())
      .map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.notFound().build());
  }

}
