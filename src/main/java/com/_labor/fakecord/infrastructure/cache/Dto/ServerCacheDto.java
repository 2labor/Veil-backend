package com._labor.fakecord.infrastructure.cache.Dto;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServerCacheDto implements Serializable {
  private Long id;
  private UUID ownerId;
  private String name;
  private String description;
  private String bannerUrl;
  private String iconUrl;
  private Integer memberCounter;
}