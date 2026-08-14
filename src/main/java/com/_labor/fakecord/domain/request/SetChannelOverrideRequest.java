package com._labor.fakecord.domain.request;

import com._labor.fakecord.domain.enums.PermissionHolderType;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SetChannelOverrideRequest(
  @NotNull 
  PermissionHolderType holderType,
  @NotNull 
  Long allowMask,
  @NotNull 
  Long denyMask
){}
