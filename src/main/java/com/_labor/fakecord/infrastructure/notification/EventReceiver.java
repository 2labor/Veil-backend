package com._labor.fakecord.infrastructure.notification;

public interface EventReceiver {
  void handleEvent(String topic, Object body);
}
