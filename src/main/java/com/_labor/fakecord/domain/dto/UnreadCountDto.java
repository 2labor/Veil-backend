package com._labor.fakecord.domain.dto;

public record UnreadCountDto(
  Long channelId,
  Long count
) {}