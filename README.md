# Veil

A production-style distributed messaging backend — started as "let me try WebSockets",
kept going because the problems got interesting.

Active development since January 2026.

---

## Architecture highlights

**Identity & Security** — JWT with HTTP-only cookies + CSRF, Google OAuth2, TOTP-based 2FA,
AES-encrypted backup codes, and transparent OAuth token encryption via JPA `AttributeConverter`.

**Real-time Messaging** — WebSocket/STOMP relay, Snowflake IDs for ordered messages,
Redis Pub/Sub for cross-node broadcast, Kafka for async notification delivery.

**Caching** — Caffeine (L1) + Redis (L2) with version-based invalidation and
outbox-triggered eviction. No full resets — targeted per entity and subtype.

**Event-driven Infra** — Transactional outbox pattern, multi-handler Kafka relay,
RabbitMQ delayed consumer for retryable background tasks.

**Social layer** — Friend system, bidirectional blocking, CQRS-based service split,
Spotify OAuth via strategy pattern, MinIO/S3 presigned uploads for avatars.

---

## Stack

Java 21 · Spring Boot 3 · PostgreSQL · Redis · Kafka · RabbitMQ · Docker · WebSocket/STOMP

---

## Status

In active development. Messaging and notifications current focus — servers next.
Documentation and architecture diagrams coming once the core stabilizes.
