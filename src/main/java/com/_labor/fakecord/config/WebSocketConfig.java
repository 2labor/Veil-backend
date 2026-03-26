package com._labor.fakecord.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com._labor.fakecord.interceptor.PresenceInterceptor;
import com._labor.fakecord.interceptor.RateLimitInterceptor;
import com._labor.fakecord.interceptor.WebSocketTokenFilter;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Autowired
  private WebSocketTokenFilter tokenFilter;

  @Autowired
  private RateLimitInterceptor rateLimitInterceptor;

  @Autowired
  private PresenceInterceptor presenceInterceptor;

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(tokenFilter, presenceInterceptor, rateLimitInterceptor);
  }

  @Override 
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableStompBrokerRelay("/topic", "/queue")
      .setRelayHost("localhost")
      .setRelayPort(61613)
      .setClientLogin("user")
      .setClientPasscode("password")
      .setSystemLogin("user")
      .setSystemPasscode("password");

    registry.setApplicationDestinationPrefixes("/app");
    registry.setUserDestinationPrefix("/user");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // handshake
    registry.addEndpoint("/ws-chat")
      .setAllowedOriginPatterns("*")
      .withSockJS();
  }

  @Bean
  public TaskScheduler heartbeatScheduler() {
    return new ThreadPoolTaskScheduler();
  }
}