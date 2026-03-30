package com._labor.fakecord.controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.MessageEditRequest;
import com._labor.fakecord.domain.dto.MessageRequest;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.MessageMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.security.ratelimit.RateLimitSource;
import com._labor.fakecord.security.ratelimit.annotation.RateLimited;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.MessageService;
import com._labor.fakecord.services.UserProfileCache;

import jakarta.validation.Valid;
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

  @PostMapping
  @RateLimited(
    key = "chat_send", 
    capacity = 5, 
    refillSeconds = 10, 
    source = RateLimitSource.AUTHENTICATED
  )
  public ResponseEntity<MessageDto> sendMessage(
    @PathVariable Long channelId,
    @RequestBody @Valid MessageRequest request,
    Principal principal
  ) {
    UUID userId = getUserId(principal);
    Message message = messageService.sendMessage(channelId, userId, request.content(), request.nonce());

    if (message == null) return ResponseEntity.ok().build();

    return ResponseEntity.ok(toDto(message));
  }
  
  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageDto> editMessage(
    @PathVariable Long channelId,
    @PathVariable Long messageId,
    @RequestBody @Valid MessageEditRequest request,
    Principal principal
  ) {
    UUID userId = getUserId(principal);

    Message message = messageService.editMessage(messageId, userId, request.content());
    return ResponseEntity.ok(toDto(message));
  }

  @GetMapping
  public ResponseEntity<List<MessageDto>> getMessages(
    @PathVariable Long channelId,
    @RequestParam(required = false) Long before,
    @RequestParam(defaultValue = "50") int limit,
    Principal principal
  ) {
    UUID userId = getUserId(principal);

    var slice = (before == null) 
      ? messageService.getLatestMessages(channelId,userId ,limit)
      : messageService.getMessagesBefore(channelId, userId, before, limit);

    return ResponseEntity.ok(mapToDtoList(slice.getContent()));
  }

  @GetMapping("/{targetMessageId}/context")
  public ResponseEntity<List<MessageDto>> getContext(
    @PathVariable Long channelId,
    @PathVariable Long targetMessageId,
    @RequestParam(defaultValue = "20") int limit,
    Principal principal
  ) {
    UUID userId = getUserId(principal);

    var messages = messageService.getMessageContent(channelId, userId, targetMessageId, limit);
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

  private UUID getUserId(Principal principal) {
    return UUID.fromString(principal.getName());
  }

  private MessageDto toDto(Message entity) {
    var fullProfile = profileCache.getUserProfile(entity.getAuthorId());
    var profile = profileMapper.toShortDto(fullProfile, UserStatus.ONLINE);
    return messageMapper.toDto(entity, profile); 
  }

  private List<MessageDto> mapToDtoList(List<Message> messages) {
    return messages.stream()
      .map(this::toDto)
      .toList();
  }
}
