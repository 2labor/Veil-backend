package com._labor.fakecord.domain.mappper.Impl;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.mappper.MessageMapper;
@Component
public class MessageMapperImpl implements MessageMapper {

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
  public MessageDto toDto(Message message, UserProfileShort authorDto) {

    if (null == message) return null;

  
    return new MessageDto(
      String.valueOf(message.getId()),
      message.getContent(),
      message.getType(),
      authorDto,
      message.getNonce(),
      message.getUpdatedAt(),
      message.getCreatedAt() 
    );
  }
  
}
