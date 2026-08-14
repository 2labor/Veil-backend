package com._labor.fakecord.domain.mappper;

import java.util.List;

import com._labor.fakecord.domain.dto.ChannelOverrideResponseDto;
import com._labor.fakecord.domain.entity.ChannelPermissionOverride;

public interface ChannelPermissionOverrideMapper {
  ChannelOverrideResponseDto toResponseDto(ChannelPermissionOverride entity);
  List<ChannelOverrideResponseDto> toResponseDtos(List<ChannelPermissionOverride> entities);
}
