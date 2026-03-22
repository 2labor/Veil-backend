package com._labor.fakecord.services;

public interface HandleGeneratorService {
  String prepareHandle(String input);
  String generateUniqueDiscriminator(String handle);
}
