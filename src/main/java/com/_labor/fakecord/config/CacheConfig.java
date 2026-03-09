package com._labor.fakecord.config;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.infrastructure.cache.Dto.CachedSlice;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

  @Bean
  public Cache<String, CachedSlice<UserProfileShort>> localCache() {
    return Caffeine.newBuilder()
        .initialCapacity(100)
        .maximumSize(5000)
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .recordStats()
        .build();
  }
}