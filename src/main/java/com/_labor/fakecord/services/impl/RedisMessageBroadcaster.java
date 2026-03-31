package com._labor.fakecord.services.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.MessageDelete;
import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.SocketEventType;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.events.SocketEvent;
import com._labor.fakecord.domain.mappper.MessageMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.infrastructure.messaging.EventPublisher;
import com._labor.fakecord.services.MessageBroadcaster;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisMessageBroadcaster implements MessageBroadcaster {
  
  private final EventPublisher eventPublisher;
  private final MessageMapper messageMapper;
  private final UserProfileCache profileCache;
  private final UserProfileMapper profileMapper;

  @Override
  public void broadcastMessageEvent(Message message, SocketEventType type) {
    log.debug("Broadcasting message event [{}] for message {}", type, message.getId());
    MessageDto messageDto = convertToDto(message);
    publish(type, message.getChannelId(), messageDto);
  }

  @Override
  public void broadcastDeletion(Long channelId, Long messageId) {
    log.debug("Broadcasting deletion for message {} in channel {}", messageId, channelId);
    publish(SocketEventType.MESSAGE_DELETE, channelId, new MessageDelete(messageId));
  }

  @Override
  public void broadcastSystemEvent(Long channelId, SocketEventType type, Object data) {
    log.debug("Broadcasting system event [{}] for channel {}", type, channelId);
    publish(type, channelId, data);
  }

  private void publish(SocketEventType type, Long channelId, Object data) {
    SocketEvent event = SocketEvent.of(type, channelId, data);
    eventPublisher.publish("chat:events", event);
  }

  private MessageDto convertToDto(Message message) {
    var fullProfile = profileCache.getUserProfile(message.getAuthorId());
    var shortProfile = profileMapper.toShortDto(fullProfile, UserStatus.ONLINE);
    return messageMapper.toDto(message, shortProfile);
  }
}