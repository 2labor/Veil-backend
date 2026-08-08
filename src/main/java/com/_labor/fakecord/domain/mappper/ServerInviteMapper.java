package com._labor.fakecord.domain.mappper;

import java.util.List;

import com._labor.fakecord.domain.dto.ServerInviteCreateDto;
import com._labor.fakecord.domain.dto.ServerInviteResponseDto;
import com._labor.fakecord.domain.entity.ServerInvite;

public interface ServerInviteMapper {
  ServerInviteResponseDto toDto(ServerInvite entity);
  List<ServerInviteResponseDto> toListDto(List<ServerInvite> serverInvites);
  ServerInvite toEntity(ServerInviteCreateDto dto);
}
