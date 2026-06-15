package com._labor.fakecord.domain.mappper.Impl;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.EmojiResponse;
import com._labor.fakecord.domain.entity.Emoji;
import com._labor.fakecord.domain.mappper.EmojiMapper;

@Component
public class EmojiMapperImpl implements EmojiMapper {

  @Override
  public EmojiResponse toPayload(Emoji entity) {
    if (entity == null) return null;

    return new EmojiResponse(
    entity.getId(), 
    entity.getName(), 
    entity.getCategory() != null ? entity.getCategory().name() : null, 
    entity.getServerId(), 
    entity.getUrl(),
    entity.isAnimated(),
    entity.getCreatedAt() != null ? entity.getCreatedAt().toEpochMilli() : null
    );
  }
  
}
