package com._labor.fakecord.domain.dto;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.Builder;

@Builder  
public record ServerDto(
  @JsonSerialize(using = ToStringSerializer.class)
  String id,
  UUID ownerId,
  String name,
  String description,
  String bannerUrl,
  String iconUrl
) {}