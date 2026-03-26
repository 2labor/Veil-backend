package com._labor.fakecord.domain.events;

import com._labor.fakecord.domain.enums.SocketEventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SocketEvent<T> {
  private SocketEventType t; // type 
  private Long c; //channelId
  private T d; // data for the event

  public static <T> SocketEvent<T> of(SocketEventType type, Long channelId, T data) {
    return new SocketEvent<>(type, channelId, data);
  }
}
