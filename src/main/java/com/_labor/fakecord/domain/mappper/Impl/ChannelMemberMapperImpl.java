package com._labor.fakecord.domain.mappper.Impl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ChannelMemberDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.domain.entity.ChannelMemberId;
import com._labor.fakecord.domain.mappper.ChannelMemberMapper;


@Component
public class ChannelMemberMapperImpl implements ChannelMemberMapper {

  @Override
  public ChannelMemberDto toDto(ChannelMember entity, UserProfileShort profileDto) {
    if (entity == null) return null;

    return ChannelMemberDto.builder()
      .profile(profileDto)
      .lastReadMessageId(entity.getLastReadMessageId())
      .joinAt(entity.getJoinAt())
      .build();
  }

  @Override
  public ChannelMember toEntity(Long channelId, UUID userId) {
    if (channelId == null || userId == null) return null;

    ChannelMemberId memberId = new ChannelMemberId(channelId, userId);

    return ChannelMember.builder()
      .id(memberId)
      .build();
  }
  
}
