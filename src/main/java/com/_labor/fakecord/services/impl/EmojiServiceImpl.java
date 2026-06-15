package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._labor.fakecord.domain.dto.EmojiResponse;
import com._labor.fakecord.domain.entity.Emoji;
import com._labor.fakecord.domain.enums.EmojiCategory;
import com._labor.fakecord.domain.mappper.EmojiMapper;
import com._labor.fakecord.repository.EmojiRepository;
import com._labor.fakecord.services.EmojiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmojiServiceImpl implements EmojiService {

  private final EmojiRepository repo;
  private final EmojiMapper mapper;

  @Override
  @Transactional(readOnly = true)
  public List<EmojiResponse> getGlobalEmojisByCategory(EmojiCategory category) {
    List<Emoji> emojis = repo.findByServerIdIsNullAndCategory(category);

    return emojis.stream()
      .map(mapper::toPayload)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<EmojiResponse> getGlobalEmojiByCategoryAndName(EmojiCategory category, String name) {
    Optional<Emoji> emoji = repo.findByServerIdIsNullAndNameAndCategory(name, category);
    return emoji.map(mapper::toPayload);
  }
}
