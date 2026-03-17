package com._labor.fakecord.infrastructure.outbox.domain.enums;

public enum CacheSubType {
  NONE("none"),
  INCOMING_LIST("incoming_list"),
  OUTGOING_LIST("outgoing_list"),
  INCOMING_COUNTER("incoming_counter"),
  OUTGOING_COUNTER("outgoing_counter"),
  BLOCK_LIST("block_list"),
  BLOCK("block"),
  ALL("all");

  private String name;

  CacheSubType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
