package com._labor.fakecord.services.impl;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com._labor.fakecord.domain.dto.UnreadCountDto;
import com._labor.fakecord.domain.entity.ChannelMember;
import com._labor.fakecord.repository.ChannelMemberRepository;
import com._labor.fakecord.repository.MessageRepository;
import com._labor.fakecord.services.UnreadCounterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UnreadCounterServiceImpl implements UnreadCounterService{

    private final RedisTemplate<String, Object> redisTemplate;
    private final MessageRepository repository;
    private final ChannelMemberRepository memberRepository;
    private final String UNREAD_KEY_PREFIX = "unread:";

    @Override
    public void increment(Long channelId, UUID userId) {
        redisTemplate.opsForHash().increment(
            getRedisKey(userId),
            channelId.toString(),
            1
        );
    }

    @Override
    public void reset(Long channelId, UUID userId) {
        redisTemplate.opsForHash().put(
            getRedisKey(userId),
            channelId.toString(),
            0
        );
    }

    @Override
    public Map<Long, Integer> getAllCounters(UUID userId) {
        Map<Object, Object> entities = redisTemplate.opsForHash().entries(getRedisKey(userId));

        if (entities.isEmpty()) {
            log.info("Unread cache miss for user {}. Needs sync.", userId);
            return Map.of();
        }

        return entities.entrySet().stream()
            .collect(Collectors.toMap(
                e -> Long.valueOf(e.getKey().toString()),
                e -> ((Number) e.getValue()).intValue()
            ));
    }

    @Override
    public int getCounter(Long channelId, UUID userId) {
        Object counter = redisTemplate.opsForHash().get(getRedisKey(userId), channelId.toString());
        if (counter == null) return -1;

        if (counter instanceof Integer i) {
            return i;
        }

        try {
            return Integer.parseInt(counter.toString());
        } catch (NumberFormatException e) {
            log.error("Invalid counter format in Redis for user {} and channel {}: {}", userId, channelId, counter);
            return 0;
        }
    }

    @Override
    public void synchFromDb(UUID userId, Map<Long, Long> channelLastReadIds) {

        List<UnreadCountDto> unreadCounts = repository.countUnreadForChannels(
            userId, 
            new ArrayList<>(channelLastReadIds.keySet())
        );

        Map<String, Object> redisMap = unreadCounts.stream()
            .collect(Collectors.toMap(
                dto -> dto.channelId().toString(),
                dto -> dto.count().intValue()
            ));
        
        channelLastReadIds.keySet().forEach(id -> {
            if (!redisMap.containsKey(id.toString())) {
                redisMap.put(id.toString(), 0);
            }
        });

        String key = getRedisKey(userId);
        redisTemplate.opsForHash().putAll(key, redisMap);

        redisTemplate.expire(key, Duration.ofDays(7));

        log.info("Synced {} unread counters from DB for user {}", redisMap.size(), userId);
    }

    @Override
    public void syncSpecific(UUID userId, Long channelId, int counter) {
        String key = getRedisKey(userId);

        redisTemplate.opsForHash().put(key, channelId.toString(), String.valueOf(counter));
    }

    
    @Override
    public void syncAllFromDb(UUID userId) {
        log.info("Starting full unread count synchronization for user {}", userId);

        List<ChannelMember> memberships = memberRepository.findAllById_UserId(userId);

        String redisKey = getRedisKey(userId);

        for (ChannelMember member : memberships) {
            Long channelId = member.getId().getChannelId();
            Long lastReadId = member.getLastReadMessageId();

            Long startId = (lastReadId != null) ? lastReadId : 0L;

            long counter = repository.countByChannelIdAndIdGreaterThan(channelId, startId);

            if (counter > 0) {
                redisTemplate.opsForHash().put(redisKey, channelId.toString(), String.valueOf(counter));
            } else {
                redisTemplate.opsForHash().delete(redisKey, channelId.toString());
            }  
        }
        log.info("Synchronization completed for user {}. Processed {} channels.", userId, memberships.size());
    }  
    
    private String getRedisKey(UUID userId) {
        return UNREAD_KEY_PREFIX + userId;
    }
}
