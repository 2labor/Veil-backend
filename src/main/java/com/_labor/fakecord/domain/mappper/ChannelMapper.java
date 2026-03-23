package com._labor.fakecord.domain.mappper;

import java.util.List;

import com._labor.fakecord.domain.dto.ChannelDto;
import com._labor.fakecord.domain.dto.DirectMessageChannelDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Channel;

public interface ChannelMapper {
  ChannelDto toDto(Channel entity);
  Channel fromDto(ChannelDto dto);
  DirectMessageChannelDto toDirectDto(Channel entity, UserProfileShort recipient, int unreadCount);
  List<ChannelDto> toDtoList(List<Channel> channels);
}

