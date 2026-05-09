package com._labor.fakecord.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.AttachmentDto;
import com._labor.fakecord.domain.dto.AttachmentTask;
import com._labor.fakecord.domain.dto.MessageContext;
import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.ReplyPreviewDto;
import com._labor.fakecord.domain.dto.UserProfileFullDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Attachment;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.AttachmentType;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.AttachmentMapper;
import com._labor.fakecord.domain.mappper.MessageMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.infrastructure.outbox.domain.payload.MediaTaskPayload;
import com._labor.fakecord.infrastructure.outbox.domain.payload.MessageCreatedPayload;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.MessageEnricher;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageEnricherImpl implements MessageEnricher {
  
  private final MessageMapper messageMapper;
  private final AttachmentMapper attachmentMapper;
  private final UserProfileCache profileCache;
  private final UserProfileMapper userProfileMapper;
  private final MessageRepository messageRepository;

  @Override
  public MessageDto enricher(Message message) {

    UserProfileFullDto userProfile = profileCache.getUserProfile(message.getAuthorId());
    UserProfileShort profileShortDto = userProfileMapper.toShortDto(userProfile, UserStatus.ONLINE);
    
    ReplyPreviewDto preview = null;
    if (message.getParentId() != null) {
      preview = messageRepository.findById(message.getParentId())
        .map(this::buildReplyPreview)
        .orElse(null);
    }

    return messageMapper.toDto(message, profileShortDto, preview);
  }

  @Override
  public List<MessageDto> enrichBatch(List<Message> messages) {
    if (messages == null || messages.isEmpty()) return List.of();

    Set<Long> parentIds = messages.stream()
      .map(Message::getParentId)
      .filter(Objects::nonNull)
      .collect(Collectors.toSet());
    
    Map<Long, ReplyPreviewDto> preloadedPreviews = Map.of();
    if (!parentIds.isEmpty()) {
      preloadedPreviews = messageRepository.findAllById(parentIds).stream()
        .collect(Collectors.toMap(
          Message::getId,
          this::buildReplyPreview
        ));
    }

    final Map<Long, ReplyPreviewDto> finalPreviews = preloadedPreviews;
    return messages.stream()
      .map(message -> {
        var fullProfile = profileCache.getUserProfile(message.getAuthorId());
        UserProfileShort authorDto = userProfileMapper.toShortDto(fullProfile, UserStatus.ONLINE);
        
        ReplyPreviewDto preview = (message.getParentId() != null) ? finalPreviews.get(message.getParentId()) : null;
        
        return messageMapper.toDto(message, authorDto, preview);
      })
      .collect(Collectors.toList());
  }

  @Override
  public MessageCreatedPayload createPayload(Message saved, MessageContext context, String authorName) {
    List<AttachmentDto> attachmentsDto = attachmentMapper.toListDto(saved.getAttachments());

    return new MessageCreatedPayload(
      saved.getId(),
      saved.getChannelId(),
      context.getServiceId(),
      saved.getAuthorId(),
      authorName,
      saved.getContent(),
      saved.getParentId(),
      attachmentsDto,
      null,
      System.currentTimeMillis(),
      context.getChannelType(),
      context.getChannelName()
    );
  }
  
  @Override
  public MediaTaskPayload createMediaTaskPayload(UUID ownerId, List<Attachment> attachments) {
    if (attachments == null || attachments.isEmpty()) return new MediaTaskPayload(ownerId, List.of());

    List<AttachmentTask> tasks = attachments.stream()
      .map(a -> new AttachmentTask(
        a.getId(),
        a.getStorageName(),
        a.getAttachmentType(),
        a.getContentType()
      ))
      .toList();

      log.info("Enricher mapped {} attachments to {} tasks", attachments.size(), tasks.size());

      return new MediaTaskPayload(ownerId, tasks);
  }

  @Override
  public ReplyPreviewDto buildReplyPreview(Message parent) {
    if (parent == null) return null;
    try {
      UserProfileFullDto profile = profileCache.getUserProfile(parent.getAuthorId());
        return new ReplyPreviewDto(
          profile.displayName(),
          profile.avatarUrl(),
          parent.getContent()
        );
    } catch (Exception e) {
      log.error("Failed to build reply preview for message {}", parent.getId(), e);
      String content = parent.getContent();
      String truncated = (content != null && content.length() < 25) ?  content : content.substring(0, 22) + "...";
      return new ReplyPreviewDto("Unknown User", null, truncated);
    }

  }
}
