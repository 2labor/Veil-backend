package com._labor.fakecord.services;

import java.util.Map;
import java.util.UUID;

public interface UnreadCounterService {
  void increment(Long channelId, UUID userId);
  void reset(Long channelId, UUID userId);
  Map<Long, Integer> getAllCounters(UUID userId);
  int getCounter(Long channelId, UUID userId);
  void synchFromDb(UUID userId, Map<Long, Long> channelLastReadIds);
  void syncSpecific(UUID userId, Long channelId, int counter);
  void syncAllFromDb(UUID userId);
}
