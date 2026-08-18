package com._labor.fakecord.controller;

import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.ServerDto;
import com._labor.fakecord.domain.dto.ServerPositionUpdateDto;
import com._labor.fakecord.domain.dto.ServerSidebarResponseDto;
import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.domain.enums.ServerRolePermissions;
import com._labor.fakecord.domain.mappper.ServerMapper;
import com._labor.fakecord.domain.request.CreateServerRequest;
import com._labor.fakecord.domain.request.UpdateServerRequest;
import com._labor.fakecord.security.permissions.RequirePermission;
import com._labor.fakecord.services.ServerDomainService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/v1/servers")
public class ServerController {
  private final ServerDomainService service;
  private final ServerMapper mapper;

  @PostMapping
  public ResponseEntity<ServerDto> createServer(
    @Valid @RequestBody CreateServerRequest request,
    Principal principal
  ) {
    Server server = service.createServer(getUserId(principal), request.name(), request.iconUrl());
    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(server));
  }

  @GetMapping
  public ResponseEntity<List<ServerSidebarResponseDto>> getServer(
    Principal principal
  ) {
    List<ServerSidebarResponseDto> dtos = service.getUserServers(getUserId(principal)).stream()
      .map(mapper::toSideDto)
      .toList();
    
    return ResponseEntity.ok(dtos);
  }

  @PutMapping("/position")
  public ResponseEntity<Void> updateServersPosition(
    Principal principal,
    @Valid @RequestBody List<ServerPositionUpdateDto> positionsDto
  ) {
    UUID userId = getUserId(principal);

    Map<Long, Integer> positions = positionsDto.stream().collect(Collectors.toMap(
      ServerPositionUpdateDto::serverId,
      ServerPositionUpdateDto::position,
      (existing, replacement) -> replacement
    ));

    service.updateServerPositions(userId, positions);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{serverId}")
  public ResponseEntity<ServerDto> getServerById(
    Principal principal,
    @PathVariable Long serverId
  ) {
    UUID userId = getUserId(principal);

    return ResponseEntity.ok(mapper.toDto(service.getServerById(userId, serverId)));
  }

  @PutMapping("/{serverId}")
  @RequirePermission(ServerRolePermissions.MANAGE_SERVER)
  public ResponseEntity<ServerDto> updateServer(
    Principal principal, 
    @PathVariable Long serverId,
    @Valid @RequestBody UpdateServerRequest request
  ) {
    UUID userId = getUserId(principal);

    Server updateServer = mapper.toEntity(request);
    
    return ResponseEntity.ok(mapper.toDto(service.updateServer(userId, serverId, updateServer)));
  }

  @DeleteMapping("/{serverId}")
  public ResponseEntity<Void> deleteServer(
    Principal principal,
    @PathVariable Long serverId
  ) {
    UUID userId = getUserId(principal);

    service.deleteServer(userId, serverId);

    return ResponseEntity.noContent().build();
  }

  private UUID getUserId(Principal principal) {
    if (principal == null) {
      throw new IllegalStateException("Security principal is missing");
    }
    return UUID.fromString(principal.getName()); 
  }
}
