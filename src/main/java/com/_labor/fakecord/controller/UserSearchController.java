package com._labor.fakecord.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.services.UserSearchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/user-search")
@RequiredArgsConstructor
@Slf4j
public class UserSearchController {
  
  private final UserSearchService service;

  @GetMapping("/profile")
  public ResponseEntity<UserProfileShort> searchByTag(@RequestParam String query) {
    UserProfileShort response = service.searchByFullTag(query);
    return ResponseEntity.ok(response);
  }

}
