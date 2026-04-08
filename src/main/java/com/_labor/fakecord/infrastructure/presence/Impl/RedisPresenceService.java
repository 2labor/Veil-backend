package com._labor.fakecord.infrastructure.presence.Impl;


import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.infrastructure.presence.PresenceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisPresenceService implements PresenceService {

  private final RedisTemplate<String, Object> redisTemplate;
  private static final String PRESENCE_KEY = "presence:users:location"; 
  private static final String CHANNEL_MEMBERS_PREFIX = "presence:channel:members:";
  private static final String VIEWERS_PREFIX = "presence:viewers:";

  @Override
  public void enterChannel(UUID userId, Long channelId) {
    redisTemplate.opsForHash().put(PRESENCE_KEY, userId.toString(), channelId.toString());
    
    String channelCache = CHANNEL_MEMBERS_PREFIX + channelId;
    redisTemplate.opsForSet().add(channelCache, userId.toString());
  }

  @Override
  public void leaveChannel(UUID userId) {
    getCurrentChannel(userId).ifPresent(channelId -> {
      redisTemplate.opsForSet().remove(CHANNEL_MEMBERS_PREFIX + channelId, userId.toString());
    });
    
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

  @Override
  public Stream<UUID> streamChannelMembers(Long channelId) {
    Set<Object> members = redisTemplate.opsForSet().members(CHANNEL_MEMBERS_PREFIX + channelId);
    return convertToStream(members);
  }

  @Override
  public void addViewer(UUID targetId, UUID observerId) {
    String key = VIEWERS_PREFIX + targetId;

    redisTemplate.opsForSet().add(key, observerId.toString());
    redisTemplate.expire(key, Duration.ofMinutes(30));
  }

  @Override
  public void removeViewer(UUID targetId, UUID observerId) {
    String key = VIEWERS_PREFIX + targetId;
    redisTemplate.opsForSet().remove(key, observerId.toString());
  }

  @Override
  public Stream<UUID> streamActiveViewers(UUID targetId) {
    Set<Object> viewers = redisTemplate.opsForSet().members(VIEWERS_PREFIX + targetId);
    return convertToStream(viewers);
  }

  private Stream<UUID> convertToStream(Set<Object> set) {
    if (set == null || set.isEmpty()) {
      return Stream.empty();
    }
    return set.stream()
      .map(obj -> UUID.fromString(obj.toString()));
  }

}
