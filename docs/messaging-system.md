# Design Document: Messaging & Notification System

This document outlines the architecture, data model, security pipeline, and real-time delivery logic for the Messaging & Notification System, designed for high-performance messaging environments.

## 1. High-Level Design Goals
* **Channel-First Unification:** DMs and group chats share one schema — a DM is a private `Channel` with two members.
* **Time-Ordered IDs:** TSID (64-bit) replaces UUID v4 for monotonic B-Tree inserts and intrinsic chronological sorting.
* **Security-at-the-Gate:** Every write validates channel membership and social graph blocks before any DB interaction.
* **Idempotent Writes:** Client-supplied nonces prevent duplicate messages during network retries.
* **Functional Presence Routing:** `DND` status suppresses push/sound but always increments the unread counter.
* **Scalability:** KKV schema mirrors the Cassandra data model, enabling a future migration without query rewrites.

---

## 2. Data Models

### Table: `channels`
Represents any conversation context — DM or group.
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | TSID (BIGINT) | Primary key, encodes creation time |
| `type` | Enum | `DIRECT`, `GROUP`, `SERVER` |
| `name` | VARCHAR(100) | `NULL` for `DIRECT` channels |
| `owner_id` | BIGINT | FK → `users.id`; admin of group/server channels |
| `slow_mode_ms` | INT | Per-user rate limit window in milliseconds |
| `created_at` | TIMESTAMPTZ | Creation timestamp |

### Table: `channel_members`
Many-to-many access control table. The composite PK is the primary membership lookup index.
| Field | Type | Description |
| :--- | :--- | :--- |
| `channel_id` | BIGINT | FK → `channels.id` (composite PK, part 1) |
| `user_id` | BIGINT | FK → `users.id` (composite PK, part 2) |
| `role` | Enum | `OWNER`, `ADMIN`, `MEMBER` |
| `joined_at` | TIMESTAMPTZ | Join timestamp |
| `last_read_id` | BIGINT | TSID of the last message read by this user |

### Table: `messages`
Core KKV storage. `channel_id` is the partition key; `id` (TSID) is the clustering/sort key.
| Field | Type | Description |
| :--- | :--- | :--- |
| `channel_id` | BIGINT | Partition key (K1) — composite PK part 1 |
| `id` | TSID (BIGINT) | Clustering key (K2) — composite PK part 2 |
| `sender_id` | BIGINT | FK → `users.id` |
| `content` | TEXT | Max 4,000 characters |
| `content_type` | Enum | `TEXT`, `IMAGE`, `FILE` |
| `client_nonce` | VARCHAR(64) | Idempotency token, unique per `(channel_id, sender_id)` |
| `edited_at` | TIMESTAMPTZ | `NULL` if never edited |
| `deleted_at` | TIMESTAMPTZ | Soft delete timestamp |

### Table: `message_outbox`
Transactional outbox for decoupled real-time delivery. Written atomically alongside `messages`.
| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | BIGSERIAL | Auto-incrementing PK |
| `channel_id` | BIGINT | Target channel |
| `message_id` | BIGINT | FK → `messages.id` |
| `event_type` | VARCHAR(32) | `MESSAGE_CREATED`, `MESSAGE_EDITED`, `MESSAGE_DELETED` |
| `payload` | JSONB | Full message snapshot for relay |
| `processed_at` | TIMESTAMPTZ | `NULL` until relay completes |
| `attempts` | INT | Retry counter for failure tracking |

### Table: `channel_notifications`
Tracks per-user unread state and mute preferences.
| Field | Type | Description |
| :--- | :--- | :--- |
| `channel_id` | BIGINT | Composite PK part 1 |
| `user_id` | BIGINT | Composite PK part 2 |
| `unread_count` | INT | Total unread messages |
| `mention_count` | INT | Unread direct mentions |
| `muted` | BOOLEAN | Whether notifications are suppressed |
| `muted_until` | TIMESTAMPTZ | Expiry for timed mutes; `NULL` = permanent |

---

## 3. ID Strategy: TSID over UUID v4

### Why UUID v4 fails at scale
UUID v4 is randomly distributed across the 128-bit space. As a B-Tree primary key this causes random page scatter on every `INSERT`, constant index fragmentation, and requires a separate `created_at` index for chronological sorting.

### TSID structure
A TSID is a **64-bit integer**: the upper 42 bits encode Unix milliseconds (~139-year range); the lower 22 bits hold a random suffix (up to 4M unique IDs/ms per node).

| Dimension | UUID v4 | TSID (64-bit) |
| :--- | :--- | :--- |
| Storage | 16 bytes | 8 bytes (50% saving) |
| B-Tree insert pattern | Random page scatter | Monotonic — rightmost leaf append |
| Chronological sort | Requires `created_at` index | Intrinsic — `ORDER BY id` IS `ORDER BY time` |
| Index fragmentation | High — constant page splits | Minimal — sequential inserts |
| Human-readable | Opaque hex | Crockford Base32 encodeable |

### Critical query implication
The KKV composite PK `(channel_id, id)` makes cursor pagination a pure B-Tree range scan with no filesort:
```sql
SELECT * FROM messages
WHERE  channel_id = :channelId
  AND  id < :cursor
  AND  deleted_at IS NULL
ORDER  BY id DESC
LIMIT  50;
-- Execution: Index Scan Backward on PK (channel_id, id). No heap sort.
```

---

## 4. Security & Validation Pipeline

Every write passes through a sequential gate chain. Failure at any gate aborts immediately — no partial writes occur.

| Gate | Check | Failure |
| :--- | :--- | :--- |
| **G-1: Auth** | JWT signature and expiry | `401 Unauthorized` |
| **G-2: Membership** | `SELECT 1 FROM channel_members WHERE (channel_id, user_id) = (?, ?)` | `403 Forbidden` |
| **G-3: Block Check** | Social Graph lookup — has any recipient blocked the sender? | `403 Forbidden` (opaque) |
| **G-4: Idempotency** | `INSERT ... ON CONFLICT (channel_id, sender_id, client_nonce) DO NOTHING` | `200 OK` — returns existing message |
| **G-5: Rate Limit** | Redis `ZADD` window check against `slow_mode_ms` | `429 Too Many Requests` |
| **G-6: Content** | Max 4,000 chars, `content_type` whitelist, no NUL bytes | `400 Bad Request` |
| **G-7: Write** | Atomic `INSERT messages` + `INSERT message_outbox` | `500 Internal Server Error` |

### 4.1. Block Check — Social Graph Integration
* For `DIRECT` channels: one lookup — `SELECT 1 FROM user_blocks WHERE blocker_id = :recipientId AND blocked_id = :senderId`.
* For `GROUP` channels: the check is scoped to direct sender↔recipient relationships only. A blocked user cannot message someone who blocked them but can still participate in shared channels where no block exists.
* **Opaque response:** The `403` for a block is intentionally identical to a membership failure to prevent senders from inferring they have been blocked.

### 4.2. Idempotency via `client_nonce`
* The client generates a unique nonce per message **before** sending (UUID v4 or TSID).
* Scoped to `(channel_id, sender_id)` and enforced at DB level via a unique index.
* On retry, the duplicate is discarded silently and the server returns `200 OK` with the original payload.
* Nonces are **not** reusable. A new message always requires a new nonce.
* A scheduled job purges nonces older than 7 days to bound table growth.

### 4.3. Transactional Integrity
* The `INSERT` into `messages` and `INSERT` into `message_outbox` execute in a **single `@Transactional` boundary**.
* If the outbox write fails, the message write rolls back — the relay never fires for a non-existent message.
* This is the same atomic guarantee as the `acceptRequest` / `blockUser` pattern in the Social Graph system.

---

## 5. Key Features & Logic

### 5.1. Real-Time Delivery via Outbox Relay
The HTTP handler never touches the WebSocket layer. After the transactional write, a relay process tails the outbox:
1. `SELECT` unprocessed rows in batches of 100.
2. Resolve channel members from Redis or DB.
3. Apply Presence filtering per recipient (see §5.2).
4. Emit WebSocket frame and/or push notification.
5. `UPDATE message_outbox SET processed_at = now()`.

**Relay implementation options:**

| Approach | Latency | Ops Complexity |
| :--- | :--- | :--- |
| Scheduled polling (`@Scheduled`) | ~100–500ms | Low — no extra infra |
| PostgreSQL `LISTEN / NOTIFY` | ~10–50ms | Low — DB only |
| Debezium CDC → Kafka | ~5–20ms | High — requires Kafka cluster |

> **Recommendation:** Start with `LISTEN/NOTIFY`. Migrate to Debezium/Kafka when fan-out per message exceeds ~500 recipients or volume exceeds 10K messages/sec.

### 5.2. Functional Presence — Notification Routing
When the relay resolves the recipient list, it queries the Presence System (Redis) for each recipient's current status:

| Presence Status | WebSocket Frame | Push Notification | Unread Increment |
| :--- | :--- | :--- | :--- |
| `ONLINE` | ✅ Sent | ✅ Sent | ✅ Yes |
| `IDLE` | ✅ Sent | ✅ Sent | ✅ Yes |
| `DND` | ✅ Sent | ❌ Suppressed | ✅ Yes |
| `OFFLINE` | ❌ No connection | ✅ Sent | ✅ Yes |

* **DND Rule:** The system **must** increment `unread_count` for a `DND` recipient. Suppression is UI/audio-only — a message is never silently dropped.
* The unread counter is always written regardless of presence state, ensuring consistency when the user returns online.

### 5.3. Cursor-Based Pagination
* Messages are paginated using `id` (TSID) as the cursor, not an offset.
* The client passes `before=<tsid>` to fetch the previous page; the server returns up to 50 rows in descending order.
* Offset-based pagination (`LIMIT n OFFSET m`) is explicitly forbidden — it causes a full sequential scan at deep offsets.

---

## 6. Scalability & Migration Path

### 6.1. Why PostgreSQL now
* Single writer + PgBouncer connection pool is sufficient to ~50K messages/sec on modern hardware.
* Read replicas scale history queries linearly.
* ACID transactions provide the atomic outbox guarantee with no dual-write risk.
* Low operational cost at current scale (manageable via RDS/Aurora).

### 6.2. Migration path to ScyllaDB / Cassandra
The KKV schema mirrors the Cassandra data model exactly:

| PostgreSQL Concept | Cassandra Equivalent |
| :--- | :--- |
| `PRIMARY KEY (channel_id, id)` | `PRIMARY KEY ((channel_id), id)` |
| `channel_id` (partition key) | Cassandra partition key |
| `id` TSID (clustering key) | Cassandra clustering column |
| Composite index scan | Cassandra partition read |

A future migration requires dual-writing to both stores during the cutover window, backfilling historical rows, and flipping the read path once backfill lag reaches zero. No application-level query rewrites are needed.

---

## 7. Security & Edge Case Handling

1. **Atomic Transactions:** `sendMessage` is strictly `@Transactional` — the message row and the outbox row are written together or not at all.
2. **Opaque Block Response:** The `403` for a block is indistinguishable from a membership `403`. Senders cannot determine whether they are blocked or simply not a member of the channel.
3. **Soft Deletes:** Messages use a `deleted_at` timestamp rather than hard deletion to preserve reply-threading context. A compliance purge job removes flagged rows after the retention window.
4. **Nonce Expiry:** `client_nonce` entries older than 7 days are purged on a schedule to prevent unbounded index growth while maintaining idempotency for in-flight retries.
5. **Slow Mode Enforcement:** The `slow_mode_ms` field on `channels` is enforced via a Redis sorted-set window per `(channel_id, user_id)`, keeping rate-limit state out of PostgreSQL entirely.