package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.entity.ServerMember;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.infrastructure.id.IdGenerator;
import com._labor.fakecord.repository.ServerRolesRepository;
import com._labor.fakecord.services.PermissionService;
import com._labor.fakecord.services.ServerMemberService;
import com._labor.fakecord.services.ServerRoleService;
import com._labor.fakecord.services.ServerSecurityService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerRoleServiceImpl implements ServerRoleService {
  @Value("${veil.default-role.role-name}")
  private String name;

  @Value("${veil.default-role.role-color}")
  private String hexColor;

  @Value("${veil.default-role.role-position}")
  private Integer position;

  private final ServerRolesRepository repository;
  private final IdGenerator idGenerator;
  private final PermissionService permissionService;
  private final ServerSecurityService serverSecurityService;
  private final ServerMemberService serverMemberService;

  @Transactional
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

  @Transactional
  @Override
  public ServerRole addRoleOnServer(UUID operatorId, Long serverId, ServerRole newRole) {
    permissionService.requirePermission(operatorId, serverId, ServerRolePermissions.MANAGE_ROLES);

    permissionService.requireCanGrantPermissions(operatorId, serverId, newRole.getPermissions());

    Integer rolePos = repository.findMaxPositionByServerId(serverId);
    newRole.setPosition(rolePos + 1);
    newRole.setId(idGenerator.nextId());
    newRole.setServerId(serverId);
    return repository.save(newRole);
  }

  @Transactional
  @Override
  public ServerRole updateRoleOnServer(UUID operatorId, Long serverId, ServerRole updatedRole) {
    permissionService.requirePermission(operatorId, serverId, ServerRolePermissions.MANAGE_ROLES);
    
    ServerRole existingRole = repository.findById(updatedRole.getId())
    .orElseThrow(() -> new IllegalArgumentException("No role with such id on server!"));
    
    if (!existingRole.getServerId().equals(serverId)) {
      throw new AccessDeniedException("Role with id " + existingRole.getId() + " does not belong to server " + serverId);
    }

    checkRoleHierarchy(operatorId, serverId, existingRole.getPosition());
    permissionService.requireCanGrantPermissions(operatorId, serverId, updatedRole.getPermissions());

    existingRole.setName(updatedRole.getName());
    existingRole.setHoist(updatedRole.isHoist());
    existingRole.setColorHex(updatedRole.getColorHex());
    existingRole.setPermissions(updatedRole.getPermissions());
    return repository.save(existingRole);
  }

  @Transactional
  @Override
  public void deleteRole(UUID operatorId, Long serverId, Long targetRoleId) {
    permissionService.requirePermission(operatorId, serverId, ServerRolePermissions.MANAGE_ROLES);
    
    ServerRole roleToDelete = repository.findById(targetRoleId)
      .orElseThrow(() -> new IllegalArgumentException("No role with such id: " + targetRoleId));

    if (!roleToDelete.getServerId().equals(serverId)) {
      throw new AccessDeniedException("Role with id " + roleToDelete.getId() + " does not belong to server " + serverId);
    }

    if (roleToDelete.getPosition() == 0) {
      throw new IllegalArgumentException("Cannot delete the default server role (@everyone)");
    }

    checkRoleHierarchy(operatorId, serverId, roleToDelete.getPosition());

    repository.delete(roleToDelete);
  }

  @Transactional
  @Override
  public void addRolesToMember(UUID operatorId, Long serverId, UUID targetUserId, Long targetRoleId) {
    permissionService.requirePermission(operatorId, serverId, ServerRolePermissions.MANAGE_USERS);

    ServerRole roleToAdd = repository.findById(targetRoleId)
      .orElseThrow(() -> new IllegalArgumentException("No role found with id: " + targetRoleId));

    if (!roleToAdd.getServerId().equals(serverId)) {
        throw new AccessDeniedException("Role with id " + roleToAdd.getId() + " does not belong to server " + serverId);
    }

    checkRoleHierarchy(operatorId, serverId, roleToAdd.getPosition());
    
    permissionService.requireCanGrantPermissions(operatorId, serverId, roleToAdd.getPermissions());
    ServerMember member = serverMemberService.getMemberWithRoles(targetUserId, serverId);
    member.addRole(roleToAdd);
  }

  @Transactional
  @Override
  public void removeRoleFromMember(UUID operantId, Long serverId, UUID targetUserId, Long targetRoleId) {
    permissionService.requirePermission(operantId, serverId, ServerRolePermissions.MANAGE_USERS);

    ServerRole roleToRemove = repository.findById(targetRoleId)
      .orElseThrow(() -> new IllegalArgumentException("No role with id: " + targetRoleId));

    if (!roleToRemove.getServerId().equals(serverId)) {
        throw new AccessDeniedException("Role with id " + roleToRemove.getId() + " does not belong to server " + serverId);
    }    

    checkRoleHierarchy(operantId, serverId, roleToRemove.getPosition());

    ServerMember member = serverMemberService.getMemberWithRoles(targetUserId, serverId);
    member.removeRole(roleToRemove);
  }

  @Override
  public List<ServerRole> getAllServerRoles(UUID operatorId, Long serverId) {
    if (!serverMemberService.checkIsUserMember(serverId, operatorId)) throw new AccessDeniedException("You are not a member of this server");

    permissionService.requirePermission(operatorId, serverId, ServerRolePermissions.MANAGE_ROLES);
    
    return repository.findByServerIdOrderByPositionDesc(serverId);
  }

  @Override
  public ServerRole getRoleById(UUID operatorId, Long serverId, Long roleId) {
    if(!serverMemberService.checkIsUserMember(serverId, operatorId)) throw new AccessDeniedException("You are not a member of this server");

    permissionService.requirePermission(operatorId, serverId, ServerRolePermissions.MANAGE_ROLES);

    ServerRole role = repository.findById(roleId)
      .orElseThrow(() -> new IllegalArgumentException("No role with such id: " + roleId));

    if (!role.getServerId().equals(serverId)) throw new AccessDeniedException("Role with id " + role.getId() + " does not belong to server " + serverId);

    return role;
  }

  private void checkRoleHierarchy(UUID userId, Long serverId, Integer targetRolePosition) {
    if (serverSecurityService.isUserOwner(userId, serverId)) return;

    Integer userRolePosition = serverMemberService.getMemberMaxRolePosition(userId, serverId);
    if (userRolePosition <= targetRolePosition) {
      throw new AccessDeniedException("You cannot manage a role that is higher or equal to your highest role in hierarchy");
    }
  }
}