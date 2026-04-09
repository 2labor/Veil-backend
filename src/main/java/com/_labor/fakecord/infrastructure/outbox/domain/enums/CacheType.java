package com._labor.fakecord.infrastructure.outbox.domain.enums;

public enum CacheType {
  FRIENDS("friends", "friends:"),
  BLOCKS("blocks", "social:blocks:"),
  BLOCKED_BY("blocked_by", "blocked_by:"),
  BLOCKED_LIST("blocked_list", "blocked_list:"),
  REQUESTS_INCOMING("requests-incoming", "requests-incoming:"),
  REQUESTS_ONGOING("requests-ongoing", "requests-ongoing:"),
  USER_PROFILES("user-profile", "user-profile"),
  ALL("all", "all"),
  REQUEST_COUNTER("request-counter", "request:");

  private String name;
  private String prefix;

  CacheType(String name, String prefix) {
    this.name = name;
    this.prefix = prefix;
  }

  public String getName() { return name; }
  public String getPrefix() { return prefix; }

  public static CacheType fromString(String name) {
    for (CacheType type : values()) {
      if (type.getName().equalsIgnoreCase(name)) return type;
    }
    throw new IllegalArgumentException("Unknown cache: " + name);
  }
}
