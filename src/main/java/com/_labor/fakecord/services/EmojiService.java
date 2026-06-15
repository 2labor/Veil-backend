package com._labor.fakecord.services;

import java.util.List;
import java.util.Optional;

import com._labor.fakecord.domain.dto.EmojiResponse;
import com._labor.fakecord.domain.enums.EmojiCategory;

public interface EmojiService {
  List<EmojiResponse> getGlobalEmojisByCategory(EmojiCategory category);
  Optional<EmojiResponse> getGlobalEmojiByCategoryAndName(EmojiCategory category, String name);
}