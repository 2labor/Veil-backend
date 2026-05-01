package com._labor.fakecord.domain.mappper.Impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.ReplyPreviewDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.MessageMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class MessageMapperImpl implements MessageMapper {

  private final UserProfileCache profileCache;
  private final UserProfileMapper profileMapper;

  @Override
  public Message fromDto(MessageDto dto) {
    if (null == dto) return null;

   return Message.builder()
    .id(dto.id() != null ? Long.parseLong(dto.id()) : null)
    .content(dto.content())
    .type(dto.type())
    .build();
  }

  @Override
  public MessageDto toDto(Message message, UserProfileShort authorDto, ReplyPreviewDto parentPreview) {

    if (null == message) return null;

  
    return new MessageDto(
      String.valueOf(message.getId()),
      message.getContent(),
      message.getType(),
      authorDto,
      message.getParentId(),
      parentPreview,
      message.getNonce(),
      (message.getUpdatedAt() == null ? null : message.getUpdatedAt().toEpochMilli()),
      message.getCreatedAt().toEpochMilli()
    );
  }

  @Override
  public List<MessageDto> toListDto(List<Message> messages, Map<Long, ReplyPreviewDto> preloadedPreviews) {
    if (messages == null) return null;
    
    return messages.stream()
      .map(message -> {
        var fullProfile = profileCache.getUserProfile(message.getAuthorId());
        UserProfileShort authorDto = profileMapper.toShortDto(fullProfile, UserStatus.ONLINE);
        
        ReplyPreviewDto preview = (message.getParentId() != null) ? preloadedPreviews.get(message.getParentId()) : null;
        return toDto(message, authorDto, preview);
      })
      .collect(Collectors.toList());
    }
}
