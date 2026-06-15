package com._labor.fakecord.domain.mappper;

import com._labor.fakecord.domain.dto.EmojiResponse;
import com._labor.fakecord.domain.entity.Emoji;

public interface EmojiMapper {
  EmojiResponse toPayload(Emoji entity);
}