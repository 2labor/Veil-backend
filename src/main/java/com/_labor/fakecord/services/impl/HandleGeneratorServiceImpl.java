package com._labor.fakecord.services.impl;

import java.util.Random;

import org.springframework.stereotype.Service;

import com._labor.fakecord.repository.UserProfileRepository;
import com._labor.fakecord.services.HandleGeneratorService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HandleGeneratorServiceImpl implements HandleGeneratorService{

  private final UserProfileRepository repository;
  private final Random random = new Random();

  @Override
  public String prepareHandle(String input) {
    if (input == null || input.isBlank()) return "user";

    String slug = input.toLowerCase()
      .replaceAll("[^a-z0-9]", "");

    if (slug.isEmpty()) return "user";

    return slug.substring(0, Math.min(slug.length(), 24));
  }

  @Override
  public String generateUniqueDiscriminator(String handle) {
    String tag;
    long attempts = 0;

    do { 
        tag = String.format("%04d", random.nextInt(10000));
        attempts++;
        if (attempts > 100) {
          throw new IllegalStateException("Too many collisions for handle: " + handle);
        }
    } while (repository.existsByHandleAndDiscriminator(handle, tag));
    return tag;
  }
}
