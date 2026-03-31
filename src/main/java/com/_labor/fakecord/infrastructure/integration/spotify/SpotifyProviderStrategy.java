package com._labor.fakecord.infrastructure.integration.spotify;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com._labor.fakecord.config.properties.SpotifyProperties;
import com._labor.fakecord.domain.dto.ConnectionStatusDto;
import com._labor.fakecord.domain.dto.spotify.SpotifyTokenResponse;
import com._labor.fakecord.domain.dto.spotify.SpotifyUserProfile;
import com._labor.fakecord.domain.entity.User;
import com._labor.fakecord.domain.entity.UserConnection;
import com._labor.fakecord.domain.enums.ConnectionProvider;
import com._labor.fakecord.domain.strategy.ConnectionProviderStrategy;
import com._labor.fakecord.infrastructure.outbox.domain.OutboxEventType;
import com._labor.fakecord.infrastructure.outbox.domain.payload.ConnectionCreatedPayload;
import com._labor.fakecord.infrastructure.outbox.service.OutboxService;
import com._labor.fakecord.repository.UserConnectionRepository;
import com._labor.fakecord.repository.UserRepository;
import com._labor.fakecord.services.UserProfileCache;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpotifyProviderStrategy implements ConnectionProviderStrategy {

  private final SpotifyProperties spotifyProperties;
  private final RedisTemplate<String, String> redisTemplate;
  private final UserRepository userRepository;
  private final UserConnectionRepository repository;
  private final SpotifyClient spotifyClient;
  private final OutboxService outboxService;
  private final UserProfileCache profileCache;

  private static final String STATE_KEY_PREFIX = "oauth_state:";
  private static final String STATE_SEPARATOR  = ".";

  @Override
  public ConnectionProvider getProvider() {
    return ConnectionProvider.SPOTIFY;
  }


  @Override
  public String buildAuthorizationUrl(UUID userId) {
    String randomToken = UUID.randomUUID().toString();

    String redisKey = STATE_KEY_PREFIX + userId;
    redisTemplate.opsForValue().set(redisKey, randomToken, Duration.ofMinutes(10));

    String state = userId.toString() + STATE_SEPARATOR + randomToken;

    log.info("Building Spotify auth URL for user: {}", userId);

    return UriComponentsBuilder.fromUriString("https://accounts.spotify.com/authorize")
      .queryParam("client_id",     spotifyProperties.clientId())
      .queryParam("response_type", "code")
      .queryParam("redirect_uri",  spotifyProperties.redirectUri())
      .queryParam("scope",         spotifyProperties.scope())
      .queryParam("state",         state)
      .build()
      .toUriString();
  }

  public void handleCallback(String code, String state) {
      log.info("Handling Spotify callback, state={}", state);

      int sep = state.indexOf(STATE_SEPARATOR);
      if (sep < 0) {
        throw new IllegalArgumentException("Malformed state parameter");
      }

      UUID userId;
      try {
        userId = UUID.fromString(state.substring(0, sep));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid userId in state parameter");
      }
      String receivedToken = state.substring(sep + 1);

      validateState(userId, receivedToken);

      SpotifyTokenResponse tokens  = spotifyClient.fetchTokens(code);
      SpotifyUserProfile   profile = spotifyClient.fetchUserProfile(tokens.accessToken());

      saveUserConnection(userId, tokens, profile);

      log.info("Spotify connected successfully for user: {}", userId);
  }

  @Override
  public void handleCallback(UUID userId, String code, String state) {
      throw new UnsupportedOperationException(
        "Use handleCallback(code, state) — userId is extracted from state automatically"
      );
  }

  private void validateState(UUID userId, String receivedToken) {
    String redisKey  = STATE_KEY_PREFIX + userId;
    String savedToken = redisTemplate.opsForValue().get(redisKey);

    if (savedToken == null) {
      log.error("OAuth state expired or not found for user: {}", userId);
      throw new SecurityException("OAuth state expired. Please try connecting again.");
    }
    if (!savedToken.equals(receivedToken)) {
      log.error("CSRF state mismatch for user: {}", userId);
      throw new SecurityException("Invalid OAuth state — possible CSRF attack.");
    }

    redisTemplate.delete(redisKey);
    log.debug("State validated and cleaned up for user: {}", userId);
  }

  private void saveUserConnection(UUID userId, SpotifyTokenResponse tokens, SpotifyUserProfile profile) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found: " + userId));

    UUID existingId = repository.findByUserAndProvider(user, ConnectionProvider.SPOTIFY)
      .map(UserConnection::getId)
      .orElse(null);

    UserConnection connection = UserConnection.builder()
      .id(existingId)
      .user(user)
      .provider(ConnectionProvider.SPOTIFY)
      .accessToken(tokens.accessToken())
      .refreshToken(tokens.refreshToken())
      .expiresAt(Instant.now().plusSeconds(tokens.expiresIn()))
      .externalId(profile.id())
      .externalName(profile.displayName())
      .showOnProfile(true)
      .metadata("{}")
      .build();

    repository.save(connection);
    
    profileCache.evict(userId);

    outboxService.publish(userId, OutboxEventType.USER_CONNECTION_CREATED,
      new ConnectionCreatedPayload(userId, ConnectionProvider.SPOTIFY, profile.id(), profile.displayName()));

    log.info("Spotify connection {} for user {}", existingId == null ? "created" : "updated", userId);
  }


  @Override
  public ConnectionStatusDto getStatus(UUID userId) {
    return repository.findByUserIdAndProvider(userId, ConnectionProvider.SPOTIFY)
      .map(c -> new ConnectionStatusDto(getProvider(), true,c.getExternalId() , c.getExternalName(), c.isShowOnProfile()))
      .orElse(new ConnectionStatusDto(getProvider(), false, null, null, false));
  }


  @Override
  @Transactional  
  public void toggleVisibility(UUID userId) {
    UserConnection conn = repository.findByUserIdAndProvider(userId, getProvider())
      .orElseThrow(() -> new IllegalStateException(getProvider() + " not connected"));
    
    conn.setShowOnProfile(!conn.isShowOnProfile());
    repository.save(conn);
    
    profileCache.evict(userId);
  }


  @Override
  @Transactional
  public void disconnect(UUID userId) {
    log.info("Disconnecting Spotify for user: {}", userId);

    UserConnection connection = repository.findByUserIdAndProvider(userId, ConnectionProvider.SPOTIFY)
      .orElseThrow(() -> new IllegalStateException("Spotify connection not found for user: " + userId));

    repository.delete(connection);

    profileCache.evict(userId);

    outboxService.publish(
      userId, 
      OutboxEventType.USER_CONNECTION_DELETED, 
      new ConnectionCreatedPayload(userId, ConnectionProvider.SPOTIFY, connection.getExternalId(), connection.getExternalName())
    );

    log.info("Spotify successfully disconnected for user: {}", userId);
  }
}