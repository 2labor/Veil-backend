package com._labor.fakecord.services.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
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
  public void broadcast(Message message) {
    var profile = profileCache.getUserProfile(message.getAuthorId());
    UserProfileShort authDto = profileMapper.toShortDto(profile, UserStatus.ONLINE);

    MessageDto  dto = messageMapper.toDto(message, authDto);
 
    publish(SocketEventType.MESSAGE_CREATE, message.getChannelId(), dto);
    log.debug("Message {} broadcasted to {}", message.getId());
  }

  @Override
  public void broadcastSystemEvent(Long channelId, SocketEventType type, Object data) {
    log.debug("Broadcasting system event [{}] to channel {}", type, channelId);
    publish(type, channelId, data);
  }

  private void publish(SocketEventType type, Long channelId, Object data) {
    SocketEvent event = SocketEvent.of(type, channelId, data);
    eventPublisher.publish("chat:events", event);
  }
}