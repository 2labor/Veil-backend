package com._labor.fakecord.services.impl;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.MessageDto;
import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.domain.entity.Message;
import com._labor.fakecord.domain.enums.UserStatus;
import com._labor.fakecord.domain.mappper.MessageMapper;
import com._labor.fakecord.domain.mappper.UserProfileMapper;
import com._labor.fakecord.services.MessageBroadcaster;
import com._labor.fakecord.services.UserProfileCache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQMessageBroadcaster implements MessageBroadcaster {

  private final SimpMessagingTemplate messagingTemplate;
  private final MessageMapper messageMapper;
  private final UserProfileCache profileCache;
  private final UserProfileMapper profileMapper;

  @Override
  public void broadcast(Message message) {
    var profile = profileCache.getUserProfile(message.getAuthorId());
    UserProfileShort authDto = profileMapper.toShortDto(profile, UserStatus.ONLINE);

    MessageDto  dto = messageMapper.toDto(message, authDto);
    
    String destination = "/topic/channels." + message.getChannelId();
    messagingTemplate.convertAndSend(destination, dto);

    log.debug("Message {} broadcasted to {}", message.getId(), destination);
    
  }

  @Override
  public void broadcastDeletion(Long channelId, Long messageId) {
    messagingTemplate.convertAndSend("/topic/channels." + channelId + ".delete", messageId);
  }
  
}