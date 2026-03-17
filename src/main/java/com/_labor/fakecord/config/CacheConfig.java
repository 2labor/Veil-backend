package com._labor.fakecord.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com._labor.fakecord.domain.dto.UserProfileShort;
import com._labor.fakecord.infrastructure.cache.Dto.CachedSlice;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

  @Bean(name = "sliceCache")
  public Cache<String, CachedSlice<UserProfileShort>> sliceCache() {
    return Caffeine.newBuilder().maximumSize(5000).build();
  }

  @Bean(name = "counterCache")
  public Cache<String, Long> counterCache() {
    return Caffeine.newBuilder().maximumSize(1000).build();
  }

  @Bean(name = "localCache")
  public Cache<String, Object> localCache() {
    return Caffeine.newBuilder().maximumSize(1000).build();
  }
}