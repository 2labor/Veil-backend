package com._labor.fakecord.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com._labor.fakecord.repository.ServerRolesRepository;
import com._labor.fakecord.services.ServerRoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerRoleServiceIml implements ServerRoleService {
  @Value("${veil.default-role.role-name}")
  private String name;

  @Value("${veil.default-role.role-color}")
  private String hexColor;

  @Value("${veil.default-role.role-position}")
  private Integer position;

  private final ServerRolesRepository repository;
  private final IdGenerator idGenerator;

  @Override
  public ServerRole createDefaultRole(Long serverId) {
    Long roleId = idGenerator.nextId();
  
    Long rolePermissions = ServerRolePermissions.pack(List.of(
      ServerRolePermissions.READ_CHANNEL,
      ServerRolePermissions.WRITE_TO_CHANNEL,
      ServerRolePermissions.ADD_ATTACHMENTS
    ));

    ServerRole defaultRole = ServerRole.builder()
      .id(roleId)
      .serverId(serverId)
      .name(name)
      .hoist(false)
      .colorHex(hexColor)
      .permissions(rolePermissions)
      .position(position)
      .build();

    return repository.save(defaultRole);
  }
  
}
