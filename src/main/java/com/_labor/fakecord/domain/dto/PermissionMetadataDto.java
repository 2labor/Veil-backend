package com._labor.fakecord.domain.dto;

import com._labor.fakecord.domain.enums.RolePermissionCategory;
import com._labor.fakecord.domain.enums.ServerRolePermissions;

public record PermissionMetadataDto(
  String key,
  Long mask,
  String title,
  String description,
  RolePermissionCategory category,
  String permissionDisplayName
) {
  public static PermissionMetadataDto fromEnum(ServerRolePermissions rolePermissions) {
    return new PermissionMetadataDto(
      rolePermissions.name(),
      rolePermissions.getMask(),
      rolePermissions.getTitle(), 
      rolePermissions.getDescriptions(), 
      rolePermissions.getCategory(), 
      rolePermissions.getCategory().getDisplayName()
    );
  }
}