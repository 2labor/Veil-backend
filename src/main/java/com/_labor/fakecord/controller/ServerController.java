package com._labor.fakecord.controller;

import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.ServerDto;
import com._labor.fakecord.domain.dto.ServerSidebarResponseDto;
import com._labor.fakecord.domain.entity.Server;
import com._labor.fakecord.domain.mappper.ServerMapper;
import com._labor.fakecord.domain.request.CreateServerRequest;
import com._labor.fakecord.services.ServerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/servers")
public class ServerController {
  private final ServerService service;
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

  private UUID getUserId(Principal principal) {
    if (principal == null) {
      throw new IllegalStateException("Security principal is missing");
    }
    return UUID.fromString(principal.getName()); 
  }
}
