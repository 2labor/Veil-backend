package com._labor.fakecord.services.impl;

import java.util.List;

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

  private final ServerRolesRepository repository;
  private final IdGenerator idGenerator;

  @Override
  public ServerRole createDefaultName(Long serverId) {
    Long roleId = idGenerator.nextId();
    String name = "everyone";
    String hexColor = "#99AAB5";  
  
    Long rolePermissions = ServerRolePermissions.pack(List.of(
      ServerRolePermissions.READ_CHANNEL,
      ServerRolePermissions.WRITE_TO_CHANNEL,
      ServerRolePermissions.ADD_ATTACHMENTS
    ));

    ServerRole defaultRole = ServerRole.builder()
      .id(roleId)
      .serverId(serverId)
      .name(name)
      .isDisplayable(false)
      .colorHex(hexColor)
      .permissions(rolePermissions)
      .build();

    return repository.save(defaultRole);
  }
  
}
