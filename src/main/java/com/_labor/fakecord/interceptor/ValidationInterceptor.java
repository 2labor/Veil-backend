package com._labor.fakecord.interceptor;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com._labor.fakecord.domain.dto.MessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

@Component
public class ValidationInterceptor implements ChannelInterceptor {
  private final Validator validator;

  public ValidationInterceptor(Validator validator) {
    this.validator = validator;
  }

  @Autowired
  private ObjectMapper objectMapper;

  @Override
  public  Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (null != accessor && StompCommand.SEND.equals(accessor.getCommand())) {
      try {
            byte[] payload = (byte[]) message.getPayload();
            
            var dto = objectMapper.readValue(payload, MessageRequest.class);

            var violations = validator.validate(dto);

            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
        } catch (IOException e) {
            throw new MessageDeliveryException("Invalid JSON format");
        }
    }
    return message;
  }
}
