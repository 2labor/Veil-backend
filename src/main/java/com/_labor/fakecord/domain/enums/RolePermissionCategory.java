package com._labor.fakecord.domain.enums;

import lombok.Getter;

@Getter
public enum RolePermissionCategory {
  GENERAL("General Server Permissions"),
  MEMBERSHIP("Membership & Moderation"),
  TEXT_CHANNEL("Text Channel Permissions"),
  ADMINISTRATION("Advanced Administration"); 

  private final String displayName;

  RolePermissionCategory(String displayName) {
    this.displayName = displayName;
  }
}