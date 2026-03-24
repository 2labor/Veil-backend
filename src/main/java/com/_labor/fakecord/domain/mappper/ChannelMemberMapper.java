package com._labor.fakecord.domain.mappper;

import java.util.UUID;

import com._labor.fakecord.domain.dto.ChannelMemberDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ChannelMember;

public interface ChannelMemberMapper {
  ChannelMemberDto toDto(ChannelMember entity, UserProfileShort profileDto);
  ChannelMember toEntity(Long channelId, UUID userId);
}
