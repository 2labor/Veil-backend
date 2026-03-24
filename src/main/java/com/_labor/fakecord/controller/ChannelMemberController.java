package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com._labor.fakecord.domain.dto.ChannelMemberDto;
import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.domain.mappper.ChannelMemberMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.UserProfileCache;


@RestController
@RequestMapping("/api/v1/channels/{channelId}/members")
@RequiredArgsConstructor
public class ChannelMemberController {
  
  private final ChannelMemberService service;
  private final ChannelMemberMapper mapper;
  private final UserProfileCache profileCache;
  private final UserProfileMapper profileMapper;

  @GetMapping
  public ResponseEntity<Slice<ChannelMemberDto>> getMembers(
    @PathVariable Long channelId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "50") int size
  ) {
    Slice<ChannelMember> members = service.getMembers(channelId, PageRequest.of(page, size));

    Slice<ChannelMemberDto> dtos = members.map(member -> {
      UUID userId = member.getId().getUserId();

      UserProfileFullDto fullProfile = profileCache.getUserProfile(userId);

      UserProfileShort shortProfile = profileMapper.toShortDto(fullProfile, fullProfile.status());

      return mapper.toDto(member, shortProfile);
    });
    return ResponseEntity.ok(dtos);
  }

  @PostMapping("/batch")
  public ResponseEntity<Void> addMembers(
    @PathVariable Long channelId,
    @RequestBody List<UUID> userIds,
    Principal principal
  ) {
    service.addMembers(getUserId(principal), channelId, userIds);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/read/{messageId}")
  public ResponseEntity<Void> updateReadStatus(
    @PathVariable Long channelId,
    @PathVariable Long messageId,
    Principal principal
  ) {
    service.updateLastReadMessage(channelId, getUserId(principal), messageId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/leave")
  public ResponseEntity<Void> leaveChannel(
    @PathVariable Long channelId,
    Principal principal
  ) {
    service.leaveMember(channelId, getUserId(principal));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{targetId}")
  public ResponseEntity<Void> kickUser(
    @PathVariable Long channelId, 
    @PathVariable UUID targetId,
    Principal principal
  ) {
    service.kickMember(channelId, targetId,  getUserId(principal));
    return ResponseEntity.noContent().build();
  }
  
  private UUID getUserId(Principal principal) {
    return UUID.fromString(principal.getName());
  }
}
