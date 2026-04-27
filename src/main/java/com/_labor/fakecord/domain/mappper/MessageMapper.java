package com._labor.fakecord.domain.mappper;

import java.util.List;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Message;

public interface MessageMapper {
  MessageDto toDto(Message message, UserProfileShort authorDto);
  Message fromDto(MessageDto dto);
  List<MessageDto> toListDto(List<Message> messages);
}
