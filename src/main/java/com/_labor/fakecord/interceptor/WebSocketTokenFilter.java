package com._labor.fakecord.interceptor;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

import org.springframework.messaging.Message;

import com._labor.fakecord.security.versions.TokenVersionManager;
import com._labor.fakecord.services.ChannelMemberService;
import com._labor.fakecord.services.validation.ChannelAccessValidator;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketTokenFilter implements ChannelInterceptor {
    private final TokenVersionManager tokenVersionManager;
    private final ChannelMemberService memberService;
    private final ChannelAccessValidator accessValidator;

    public WebSocketTokenFilter(TokenVersionManager tokenVersionManager, ChannelMemberService memberService, ChannelAccessValidator accessValidator) {
        this.tokenVersionManager = tokenVersionManager;
        this.memberService = memberService;
        this.accessValidator = accessValidator;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (null == accessor || accessor.getCommand() == null) return message;

        switch (accessor.getCommand()) {
            case CONNECT -> handleConnection(accessor);
            case SUBSCRIBE -> {
                validateTokenVersion(accessor);
                handleSubscribe(accessor);
            }
            case SEND -> validateTokenVersion(accessor);
            default -> {}
        }
        return message;
    }

    private void handleConnection(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();

        if (user == null) {
            log.error("WS Connection denied: No user principal found");
            throw new MessageDeliveryException("MISSING_TOKEN");
        }

        String userId = user.getName(); 
        int currentVersion = tokenVersionManager.getCurrentVersion(UUID.fromString(userId));

        if (accessor.getSessionAttributes() != null) {
            accessor.getSessionAttributes().put("userId", userId);
            accessor.getSessionAttributes().put("tokenVersion", currentVersion);
        }
        
        log.debug("User {} connected to WS with version {}", userId, currentVersion);
    }

    private void validateTokenVersion(StompHeaderAccessor accessor) {
        if (accessor.getSessionAttributes() == null) return;

        String userId = (String) accessor.getSessionAttributes().get("userId");
        if (userId == null) return;

        int currentVersionInRedis = tokenVersionManager.getCurrentVersion(UUID.fromString(userId));
        Integer sessionVersion = (Integer) accessor.getSessionAttributes().get("tokenVersion");

        if (sessionVersion != null && sessionVersion < currentVersionInRedis) {
            log.warn("Blocking WS action for user {}: outdated version", userId);
            throw new MessageDeliveryException("TOKEN_OUTDATED");
        }
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) return;

        if(destination.startsWith("/topic/channels.")) {
            try {
                Long channelId = Long.parseLong(destination.substring("/topic/channels.".length()));

                String userIdStr = (String) accessor.getSessionAttributes().get("userId");
                if (userIdStr == null) throw new MessageDeliveryException("UNAUTHORIZED");
                UUID userId = UUID.fromString(userIdStr);

                accessValidator.accessValidation(channelId, userId);

                log.debug("User {} successfully subscribed to channel {}", userId, channelId);
                
            } catch (Exception e) {
                throw new MessageDeliveryException("INVALID_CHANNEL_SUBSCRIPTION");
            }
        }
    }
}