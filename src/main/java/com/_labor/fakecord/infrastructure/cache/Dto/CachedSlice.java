package com._labor.fakecord.infrastructure.cache.Dto;

import java.io.Serializable;
import java.util.List;

public record CachedSlice<T>(
  List<T> content,
  int pageNumber,
  int pageSize,
  boolean hasNext
) implements Serializable {}
