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
import com._labor.fakecord.infrastructure.outbox.service.impl.ChatEventReceiver;


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
  MessageListenerAdapter evictListenerAdapter(CacheEvictReceiver receiver) {
    MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "handleEvict");

    adapter.setSerializer(new GenericJackson2JsonRedisSerializer());

    return adapter;
  }

  @Bean
  RedisMessageListenerContainer container(
    RedisConnectionFactory connectionFactory,
    MessageListenerAdapter evictListenerAdapter, 
    MessageListenerAdapter chatListenerAdapter
  ) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    container.addMessageListener(evictListenerAdapter, new PatternTopic("cache:evict"));
    container.addMessageListener(chatListenerAdapter, new PatternTopic("chat:events"));
    container.setTaskExecutor(Executors.newFixedThreadPool(4));
    
    return container;
  }

  @Bean 
  MessageListenerAdapter chatListenerAdapter(ChatEventReceiver receiver) {
    MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "handleEvent");
    adapter.setSerializer(new GenericJackson2JsonRedisSerializer());
    return adapter;
  }
}
