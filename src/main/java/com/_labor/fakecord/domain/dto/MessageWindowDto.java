package com._labor.fakecord.domain.dto;

import java.util.List;

public record MessageWindowDto(
  List<MessageDto> messages,
  boolean hasMoreBefore,
  boolean hasMoreAfter,
  String anchorId
) {}