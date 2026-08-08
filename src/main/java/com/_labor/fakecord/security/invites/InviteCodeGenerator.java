package com._labor.fakecord.security.invites;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InviteCodeGenerator {
  @Value("${veil.server-invites.alphabet}")
  private String ALPHABET;

  @Value("${veil.server-invites.default-length}")
  private Integer DEFAULT_LENGTH;

  private final SecureRandom random = new SecureRandom();

  public String generateCode() {
    return generateCode(DEFAULT_LENGTH);
  }

  public String generateCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i <= length; i ++) {
      sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
    }

    return sb.toString();
  }

}
