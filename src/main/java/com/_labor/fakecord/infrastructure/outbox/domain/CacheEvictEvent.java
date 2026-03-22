package com._labor.fakecord.infrastructure.outbox.domain;

import java.io.Serializable;
import java.util.UUID;

import com._labor.fakecord.infrastructure.outbox.domain.enums.CacheSubType;

public record CacheEvictEvent(
  UUID aggregateId,
  String cacheName,
  CacheSubType subType,
  long timeStamp
) implements Serializable {}
