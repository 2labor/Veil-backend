package com._labor.fakecord.infrastructure.presence.Impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.presence.PresenceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisPresenceService implements PresenceService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PRESENCE_KEY = "presence:users:location"; 

  @Override
  public void enterChannel(UUID userId, Long channelId) {
    redisTemplate.opsForHash().put(PRESENCE_KEY, userId.toString(), channelId.toString());
  }

  @Override
  public void leaveChannel(UUID userId) {
    redisTemplate.opsForHash().delete(PRESENCE_KEY, userId.toString());
  }

  @Override
  public Optional<Long> getCurrentChannel(UUID userId) {
    Object cid = redisTemplate.opsForHash().get(PRESENCE_KEY, userId.toString());
    return Optional.ofNullable(cid).map(o -> Long.valueOf(o.toString()));
  }

  @Override
  public boolean isUserInChannel(UUID userId, Long channelId) {
    return getCurrentChannel(userId)
      .map(channelId::equals)
      .orElse(false); 
  }
  
}
