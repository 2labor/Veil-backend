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
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com._labor.fakecord.infrastructure.notification.Impl.RedisNotificationReceiver;
import com._labor.fakecord.infrastructure.notification.Impl.TypingReceiver;
import com._labor.fakecord.infrastructure.outbox.service.impl.CacheEvictReceiver;
  

@Configuration
public class RedisConfig {
  
  @Bean
  public RedisSerializer<String> redisSerializer() {
    return new StringRedisSerializer();
  }
  
  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, RedisSerializer<String> redisSerializer) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(redisSerializer);

    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);
    template.setHashKeySerializer(redisSerializer);

    return template;
  }

  @Bean
  MessageListenerAdapter typingListenerAdapter(TypingReceiver receiver) {
    MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "handleEvent");
    adapter.setSerializer(new GenericJackson2JsonRedisSerializer());
    return adapter;
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
    MessageListenerAdapter typingListenerAdapter,
    RedisNotificationReceiver notificationReceiver
  ) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);

    container.addMessageListener(evictListenerAdapter, new PatternTopic("cache:evict"));
    
    container.addMessageListener(notificationReceiver, new PatternTopic("channel:events:*"));
    container.addMessageListener(notificationReceiver, new PatternTopic("users:notifications:*"));
    container.addMessageListener(typingListenerAdapter, new PatternTopic("channel:typing:*"));

    container.setTaskExecutor(Executors.newFixedThreadPool(4));
    return container;
  }
}
