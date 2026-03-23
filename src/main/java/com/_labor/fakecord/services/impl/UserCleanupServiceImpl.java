package com._labor.fakecord.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com._labor.fakecord.repository.BackupCodeRepository;
import com._labor.fakecord.repository.EmailIdentityRepository;
import com._labor.fakecord.repository.UserAuthenticatorRepository;
import com._labor.fakecord.repository.UserRepository;
import com._labor.fakecord.repository.VerificationTokenRepository;
import com._labor.fakecord.services.UserCleanupService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j
public class UserCleanupServiceImpl implements UserCleanupService {

  private final EmailIdentityRepository emailIdentityRepository;
  private final VerificationTokenRepository verificationTokenRepository;
  private final BackupCodeRepository backupCodeRepository;
  private final UserAuthenticatorRepository userAuthenticatorRepository;
  private final UserRepository repository;

  public UserCleanupServiceImpl(
      EmailIdentityRepository emailIdentityRepository,
      VerificationTokenRepository verificationTokenRepository, BackupCodeRepository backupCodeRepository,
      UserAuthenticatorRepository userAuthenticatorRepository,
      UserRepository repository
    ) {
    this.emailIdentityRepository = emailIdentityRepository;
    this.verificationTokenRepository = verificationTokenRepository;
    this.backupCodeRepository = backupCodeRepository;
    this.userAuthenticatorRepository = userAuthenticatorRepository;
    this.repository = repository;
  }

  @Override
  @Transactional
  public void scrubUnverifiedUser(UUID userId) {
    log.warn("Scrubbing data for user: {}", userId);

    repository.findById(userId).ifPresentOrElse(
      user -> {
        emailIdentityRepository.deleteByUserId(userId);
        verificationTokenRepository.deleteByUserId(userId);
        backupCodeRepository.deleteByUserId(userId);
        userAuthenticatorRepository.deleteByUserId(userId);

        repository.delete(user);
        log.info("User {} scrubbed successfully", userId);
      },
      () -> log.warn("User {} already gone. Nothing to scrub.", userId)
    );
  }
}
