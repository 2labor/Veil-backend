package com._labor.fakecord.services.impl;

import java.time.Duration;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.events.TypingEvent;
import com._labor.fakecord.services.TypingService;
import com._labor.fakecord.services.UserProfileCache;
import com._labor.fakecord.services.validation.ChannelAccessValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TypingServiceImpl implements TypingService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final UserProfileCache userProfileCache;
  private final ChannelAccessValidator accessValidator;
  
  private static final String TYPING_KEY_PREFIX = "typing:";
  private static final Duration TYPING_TTL = Duration.ofSeconds(8);

  @Override
  public void handleTyping(Long channelId, UUID userId) {
    accessValidator.accessValidation(channelId, userId);
    
    String redisKey = TYPING_KEY_PREFIX + channelId + ":" + userId;

    Boolean wasSet = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", TYPING_TTL);

    if (Boolean.TRUE.equals(wasSet)) {
      broadcastTyping(channelId, userId);
    } else {
      redisTemplate.expire(redisKey, TYPING_TTL);
    }
  }
  
  @Override
  public void stopTyping(Long channelId, UUID userId) {
    String redisKey = TYPING_KEY_PREFIX + channelId + ":" + userId;

    Boolean deleted = redisTemplate.delete(redisKey);

    if (Boolean.TRUE.equals(deleted)) {
      log.trace("Typing status cleared for user {} because message was sent", userId);
    }
  } 

  private void broadcastTyping(Long channelId, UUID userId) {
    var profile = userProfileCache.getUserProfile(userId);
    String displayName = (profile != null) ? profile.displayName() : "Unknown User";

    TypingEvent event = new TypingEvent(
      channelId.toString(),
      userId.toString(),
      displayName,
      System.currentTimeMillis()
    );

    log.trace("Broadcasting typing event: user={} channel={}", displayName, channelId);
        
    redisTemplate.convertAndSend("channel:typing:" + channelId, event);
  }
}
