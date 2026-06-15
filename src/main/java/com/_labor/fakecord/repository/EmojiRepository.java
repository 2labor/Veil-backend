package com._labor.fakecord.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.Emoji;
import com._labor.fakecord.domain.enums.EmojiCategory;

public interface EmojiRepository extends JpaRepository<Emoji, Long> {
  List<Emoji> findByServerIdIsNull(); 
  List<Emoji> findByServerId(Long serverId);
  Optional<Emoji> findByServerIdAndName(Long serverId, String name);
  Optional<Emoji> findByServerIdIsNullAndName(String name);
  List<Emoji> findByServerIdIsNullAndCategory(EmojiCategory category);
  Optional<Emoji> findByServerIdIsNullAndNameAndCategory(String name, EmojiCategory category);
}
