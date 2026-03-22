# Design Document: Relationship & Social Graph System

This document outlines the architecture, state logic, and security considerations for the user relationship system (Friends & Blocking), designed for high-performance messaging environments.

## 1. High-Level Design Goals
* **Discord-Style Explicit Blocking:** Users receive clear feedback when interacting with someone who blocked them.
* **Hidden Ignore Logic:** Senders remain unaware when their friend requests are declined or ignored.
* **Privacy-First Presence:** Blocking ensures total invisibility of online status and activity.
* **Scalability:** Optimized database indexes and transactional integrity.

---

## 2. Data Models

### Table: `friend_requests`
Manages temporary states of friendship invitations.
| Field | Type | Description |
| :--- | :--- | :--- |
| `from_id` | UUID | Sender's ID (Index) |
| `to_id` | UUID | Recipient's ID (Index) |
| `status` | Enum | `PENDING`, `IGNORED` |
| `created_at` | Instant | Timestamp for sorting outgoing requests |
| `expires_at` | Instant | Flood control TTL (Current + 15 min) |

### Table: `relationships`
Stores persistent connection states. Uses a composite unique index `(user_id, target_id)`.
| Field | Type | Description |
| :--- | :--- | :--- |
| `user_id` | UUID | The Subject (Primary Actor) |
| `target_id` | UUID | The Object (Recipient of action) |
| `type` | Enum | `FRIENDS`, `BLOCKED` |
| `updated_at` | Instant | Tracking for cache invalidation |

---

## 3. State Machine & Transitions

| Action | Initial State | Actor Status (A) | Target Status (B) | UX Behavior |
| :--- | :--- | :--- | :--- | :--- |
| **Send Request** | None | `Pending (Out)` | `Pending (In)` | A sees "Pending", B sees "New Request" |
| **Accept Request** | Pending | `Friends` | `Friends` | Reciprocal link (2 rows created in `relationships`) |
| **Ignore Request** | Pending | `Pending` (Visual) | `Ignored` (Internal) | B hides request; A still sees "Pending" |
| **Block** | Any | `Blocked` | `None` (Hidden) | A sees B in Blacklist; B gets 403 on contact |
| **Unfriend** | Friends | `None` | `None` | Relationship rows deleted for both |

---

## 4. Key Features & Logic

### 4.1. Mutual Friends Calculation
* **Algorithm:** Set intersection of `FRIENDS` list for User A and User B.
* **Performance:**
    * *MVP:* SQL Join on the `relationships` table.
    * *Scale:* Redis Set intersection using `SINTER user_a_friends user_b_friends`.
* **Privacy Rule:** If `B` has blocked `A`, the mutual friends list for `A` returns empty `[]` to prevent activity leakage.

### 4.2. Presence & Privacy Filter
When User A requests the status of User B:
1.  The system checks: `relationships.find(user_id=B, target_id=A, type=BLOCKED)`.
2.  If a block exists: Return `status: OFFLINE`, `activity: null`.
3.  This is enforced at the **Service Layer** to prevent side-channel leaks.

### 4.3. Anti-Spam (Flood Control)
* When a request is marked as `IGNORED`, `expires_at` is set to `now + 15m`.
* The `sendRequest` method validates existing requests. If a request exists (even if `IGNORED`) and has not expired, the API returns a "Cooldown active" error.

---

## 5. Security & Edge Case Handling

1.  **Atomic Transactions:** Actions like `acceptRequest` and `blockUser` are strictly `@Transactional` to prevent partial data updates (e.g., friendship appearing for only one user).
2.  **Explicit Block (403 Forbidden):** To prevent confusion, the system returns a clear error message when a blocked user attempts interaction, adhering to the Discord-style UX.
3.  **Shared Channels:** In group chats, messages from blocked users are flagged as `is_blocked_content`. The client-side UI collapses these messages under a placeholder.
4.  **Enumeration Attack Protection:** Error formats for "Blocked" and "Private Profile" are unified to prevent spammers from verifying active user IDs.