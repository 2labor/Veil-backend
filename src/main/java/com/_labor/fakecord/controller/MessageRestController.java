package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.MessageMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.MessageService;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/channels/{channelId}/message")
@RequiredArgsConstructor
public class MessageRestController {

  private final MessageService messageService;
  private final ChannelMemberService memberService;
  private final MessageMapper messageMapper;
  private final UserProfileCache profileCache;
  private final UserProfileMapper profileMapper;

  @GetMapping
  public ResponseEntity<List<MessageDto>> getMessages(
    @PathVariable Long channelId,
    @RequestParam(required = false) Long before,
    @RequestParam(defaultValue = "50") int limit,
    Principal principal
  ) {
    checkAccess(channelId, principal);

    var slice = (before == null) 
      ? messageService.getLatestMessages(channelId, limit)
      : messageService.getMessagesBefore(channelId, before, limit);

    return ResponseEntity.ok(mapToDtoList(slice.getContent()));
  }

  @GetMapping("/{targetMessageId}/context")
  public ResponseEntity<List<MessageDto>> getContext(
    @PathVariable Long channelId,
    @PathVariable Long targetMessageId,
    @RequestParam(defaultValue = "20") int limit,
    Principal principal
  ) {
    checkAccess(channelId, principal);

    var messages = messageService.getMessageContent(channelId, targetMessageId, limit);
    return ResponseEntity.ok(mapToDtoList(messages));
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> deleteMessage(
    @PathVariable Long channelId,
    @PathVariable Long messageId,
    Principal principal
  ) {
    messageService.deleteMessage(messageId, UUID.fromString(principal.getName()));
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/purge")
  public ResponseEntity<Void> purgeHistory(
    @PathVariable Long channelId,
    Principal principal
  ) {
    messageService.purgeChannelHistory(channelId, UUID.fromString(principal.getName()));
    return ResponseEntity.noContent().build();
  }

  private void checkAccess(Long channelId, Principal principal) {
    UUID userId = UUID.fromString(principal.getName());

    if (!memberService.isMember(channelId, userId)) {
      throw new RuntimeException("Access Denied: Not a member of channel " + channelId);
    }
  }

  private List<MessageDto> mapToDtoList(List<Message> messages) {
    return messages.stream()
      .map(msg -> {
        var profile = profileCache.getUserProfile(msg.getAuthorId());
        var authorDto = profileMapper.toShortDto(profile, UserStatus.ONLINE);
        return messageMapper.toDto(msg, authorDto);
      })
      .toList();
  }
}
