package com._labor.fakecord.domain.mappper;

import java.util.List;
import java.util.Map;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.ReplyPreviewDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Message;

public interface MessageMapper {
  MessageDto toDto(Message message, UserProfileShort authorDto, ReplyPreviewDto parentPreview);
  Message fromDto(MessageDto dto);
  List<MessageDto> toListDto(List<Message> messages, Map<Long, ReplyPreviewDto> preloadedPreviews);
}
