package com._labor.fakecord.domain.mappper.Impl;

import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.ServerDto;
import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.domain.mappper.ServerMapper;

@Component
public class ServerMapperImpl implements ServerMapper {

  @Override
  public ServerDto toDto(Server entity) {
    return  ServerDto.builder()
      .id(entity.getId().toString())
      .ownerId(entity.getOwnerId())
      .name(entity.getName())
      .description(entity.getDescription())
      .bannerUrl(entity.getBannerUrl())
      .iconUrl(entity.getIconUrl())
      .build();
  }
  
}
