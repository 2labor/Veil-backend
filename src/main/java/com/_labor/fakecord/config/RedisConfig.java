package com._labor.fakecord.config;

import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com._labor.fakecord.infrastructure.outbox.service.impl.CacheEvictReceiver;


@Configuration
public class RedisConfig {
  
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);

    return template;
  }

  @Bean
  MessageListenerAdapter listenerAdapter(CacheEvictReceiver receiver) {
    MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "handleEvict");

    adapter.setSerializer(new GenericJackson2JsonRedisSerializer());

    return adapter;
  }

  @Bean
  RedisMessageListenerContainer container(
    RedisConnectionFactory connectionFactory,
    MessageListenerAdapter listenerAdapter
  ) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(listenerAdapter, new PatternTopic("cache:evict"));
    container.setTaskExecutor(Executors.newSingleThreadExecutor());
    
    return container;
  }
}
