package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.ChannelDto;
import com._labor.fakecord.domain.dto.DirectMessageChannelDto;
import com._labor.fakecord.domain.dto.GroupChannelDto;
import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Channel;
import com._labor.fakecord.domain.enums.ChannelType;
import com._labor.fakecord.domain.mappper.ChannelMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.ChannelService;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelController {
  
  private final ChannelService service;
  private final ChannelMemberService memberService;
  private final ChannelMapper mapper;
  private final UserProfileMapper profileMapper;
  private final UserProfileCache profileCache;

  @GetMapping("/server/{serverId}")
  public ResponseEntity<List<ChannelDto>> getServerChannel(
    @PathVariable Long serverId
  ) {
    List<Channel> channels = service.getChannelsByServer(serverId);
    return ResponseEntity.ok(mapper.toDtoList(channels));
  }

  @GetMapping("/me")
  public ResponseEntity<List<?>> getMyDirectMessage(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size,
    Principal principal
  ) {
    UUID myId = getId(principal);
    var slice = service.getUserDirectMessages(myId, PageRequest.of(page, size));

    List<?> dtos = slice.getContent().stream().map(channel -> {
      int unreadCount = memberService.getUnreadCount(channel.getId(), myId);

      if (channel.getType() == ChannelType.GROUP_DM) {
        return mapper.toGroupDto(channel, unreadCount);
      }

      UUID recipientId = memberService.getRecipientId(channel.getId(), myId);
      UserProfileFullDto profileDto = profileCache.getUserProfile(recipientId);
      UserProfileShort profileShort = profileMapper.toShortDto(profileDto, profileDto.status());

      return mapper.toDirectDto(channel, profileShort, unreadCount);
    })
    .toList();

    return ResponseEntity.ok(dtos);
  }

  @PostMapping("/server/{serverId}")
  public ResponseEntity<ChannelDto> createServerChannel(
    @PathVariable Long serverId,
    @RequestParam String name,
    @RequestParam ChannelType type,
    Principal principal
  ) {
    UUID creatorId = getId(principal);
    Channel channel = service.createChannel(serverId, creatorId, name, type);
    return ResponseEntity.ok(mapper.toDmDto(channel));
  }

  @PostMapping("/dm/{recipientId}")
  public ResponseEntity<DirectMessageChannelDto> startDM(
    @PathVariable UUID recipientId,
    Principal principal
  ) {
    UUID myId = getId(principal);
    Channel channel = service.startDirectMessage(myId, recipientId);

    UserProfileFullDto profileFull = profileCache.getUserProfile(recipientId);
    return ResponseEntity.ok(mapper.toDirectDto(channel, 
      profileMapper.toShortDto(profileFull, profileFull.status()), 0)); 
  }

  @PostMapping("/group")
  public ResponseEntity<GroupChannelDto> createDmGroup(
    @RequestBody List<UUID> participantIds,
    @RequestParam(required = false) String name,
    Principal principal
  ) {
    UUID creatorId = getId(principal);
    Channel group = service.createGroupChat(creatorId, participantIds, name);
    return ResponseEntity.ok(mapper.toGroupDto(group, 0));
  }

  @PatchMapping("/{channelId}/name")
  public ResponseEntity<Void> rename(
    @PathVariable Long channelId,
    @RequestParam String newName
  ) {
    service.renameChannel(channelId, newName);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/reorder/{serverId}")
  public ResponseEntity<Void> reorderChannels(
    @PathVariable Long serverId,
    @RequestBody List<Long> channelIds
  ) {
    service.reorderChannels(serverId, channelIds);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{channelId}") 
  public ResponseEntity<Void> deleteChannelId(
    @PathVariable Long channelId,
    Principal principal
  ) {
    UUID operatorId = getId(principal);
    service.deleteChannel(channelId, operatorId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{channelId}/transfer-ownership")
  public ResponseEntity<Void> transferOwnership(
    @PathVariable Long channelId,
    @RequestParam UUID newOwnerId,
    Principal principal
  ) {
    memberService.transferOwnership(channelId, getId(principal), newOwnerId);
    return ResponseEntity.ok().build();
  }

  private UUID getId(Principal principal) {
    return UUID.fromString(principal.getName());
  }
}