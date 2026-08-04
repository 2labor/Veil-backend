package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.ServerRoleCreateDto;
import com._labor.fakecord.domain.dto.ServerRoleResponseDto;
import com._labor.fakecord.domain.dto.ServerRoleShortDto;
import com._labor.fakecord.domain.dto.ServerRoleUpdateDto;
import com._labor.fakecord.domain.entity.ServerRole;
import com._labor.fakecord.domain.mappper.ServerRoleMapper;
import com._labor.fakecord.services.ServerRoleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/servers/{serverId}")
@RequiredArgsConstructor
public class ServerRoleController {
  
  private final ServerRoleService service;
  private final ServerRoleMapper mapper;

  @PostMapping("/roles")
  ResponseEntity<ServerRoleResponseDto> addRoleOnServer(
    @PathVariable Long serverId,
    Principal principal,
    @Valid @RequestBody ServerRoleCreateDto dto
  ) {
    UUID userId = getUserId(principal);
    ServerRole roleToCreate = mapper.toEntity(dto);
    ServerRole createdRole = service.addRoleOnServer(userId, serverId, roleToCreate);
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(createdRole));
  }  

  @PutMapping("/roles/{roleId}")
  ResponseEntity<ServerRoleResponseDto> updateRoleOnServer(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable Long roleId,
    @Valid @RequestBody ServerRoleUpdateDto dto
  ) {
    UUID userId = getUserId(principal);
    ServerRole roleToUpdate = mapper.toEntity(dto, roleId);
    ServerRole updatedRole = service.updateRoleOnServer(userId, serverId, roleToUpdate);

    return ResponseEntity.ok(mapper.toDto(updatedRole));
  }

  @GetMapping("/roles")
  ResponseEntity<List<ServerRoleShortDto>> getAllServerRolesShort(
    Principal principal,
    @PathVariable Long serverId
  ){
    UUID userId = getUserId(principal);
    List<ServerRole> roles = service.getAllServerRoles(userId, serverId);
    return ResponseEntity.ok(mapper.toShortDtoList(roles));
  }

  @GetMapping("/roles/{roleId}")
  ResponseEntity<ServerRoleResponseDto> getRoleById(
    Principal principal, 
    @PathVariable Long serverId,
    @PathVariable Long roleId
  ) {
    UUID userId = getUserId(principal);
    ServerRole role = service.getRoleById(userId, serverId, roleId);
    return ResponseEntity.ok(mapper.toDto(role));
  }

  @DeleteMapping("/roles/{roleId}")
  ResponseEntity<Void> deleteRole(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable Long roleId
  ) {
    UUID userId = getUserId(principal);
    service.deleteRole(userId, serverId, roleId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/members/{targetUserId}/roles/{roleId}")
  ResponseEntity<Void> addRoleToMember(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable UUID targetUserId,
    @PathVariable Long roleId
  ) {
    UUID userId = getUserId(principal);
    service.addRolesToMember(userId, serverId, targetUserId, roleId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/members/{targetUserId}/roles/{roleId}")
  ResponseEntity<Void> removeRoleFromMember(
    Principal principal,
    @PathVariable Long serverId,
    @PathVariable UUID targetUserId,
    @PathVariable Long roleId
  ) {
    UUID userId = getUserId(principal);
    service.removeRoleFromMember(userId, serverId, targetUserId, roleId);

    return ResponseEntity.noContent().build();
  }

  private UUID getUserId(Principal principal) {
    if (principal == null) return null;

    return UUID.fromString(principal.getName());
  }
}