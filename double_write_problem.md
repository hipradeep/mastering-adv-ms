# The Double Write Problem (Dual Write)

## The Problem
In distributed systems, the **Double Write Problem** occurs when an application needs to update two different systems (e.g., a Database and a Message Broker) as part of a single logical operation, but cannot guarantee that both updates will either succeed or fail together atomically.

### Scenario in Terminator Project
In the `OrchestratorService`, the `initiateIssueTransaction` method performs two steps:
1.  **Database Write:** Saves a `HsttSagaTransaction` entity with status `STARTED`.
2.  **Message Queue Send:** Publishes a `ReserveStockCommand` to a Kafka topic.

### The Failure Mode (Zombie Transactions)
Without proper handling, the following sequence can occur:
1.  **Step 1 Succeeds:** The database saves the transaction record.
2.  **Step 2 Fails:** The Kafka broker is down, or the network fails, or serialization fails.
3.  **Result:** The method crashes or returns an error. The database transaction (if auto-committed) remains. The system has a record saying a process started, but the event was never sent to trigger the next step. The transaction becomes a "Zombie" — it never progresses and never fails.

---

## Possible Solutions

### 1. Synchronous Send inside DB Transaction (Selected Solution)
Wrap both operations in a single Database Transaction and force the Message Queue send to be synchronous.

-   **How it works:**
    1.  Open DB Transaction (`@Transactional`).
    2.  Writes to DB.
    3.  Sends to Kafka and **waits for acknowledgement** (`.get()`).
    4.  If Kafka fails, throw RuntimeException -> DB Transaction Rolls back.
    5.  If Kafka succeeds, Commit DB Transaction.
-   **Pros:** Simple to implement. Solves the "Zombie" problem for application errors.
-   **Cons:** Increases latency (must wait for Kafka). If the app crashes *after* Kafka ack but *before* DB commit (rare), you might have a message sent but no DB record (phantom message).
-   **Status:** **Implemented** in `OrchestratorService`.

### 2. The Transactional Outbox Pattern (Best Practice)
Instead of sending to Kafka directly, write the message to a database table within the same transaction.

-   **How it works:**
    1.  Start DB Transaction.
    2.  Write Business Entity (SagaTransaction).
    3.  Write Message to an `Outbox` table (in the same DB).
    4.  Commit Transaction (Atomic guarantee).
    5.  **Separate Process (Change Data Capture or Poller):** Reads the `Outbox` table and pushes messages to Kafka asynchronously.
-   **Pros:** 100% Atomic. Decouples availability of DB and Kafka.
-   **Cons:** Complex to set up (requires CDC tool like Debezium or a polling worker).

### 3. Event Sourcing
Store the state as a sequence of events, not a current state state.

-   **How it works:** The database *is* the event log. You append an event "TransactionStarted". Listeners subscribe to this log.
-   **Pros:** Audit trail, atomic.
-   **Cons:** Major architectural shift.

### 4. 2PC (Two-Phase Commit) / Distributed Transactions
Use protocols like XA to coordinate commits between DB and Broker.

-   **Pros:** Strong consistency.
-   **Cons:** Poor performance, not supported by all brokers (Kafka does not support XA in the traditional sense), creates locking issues.

---

## Summary of Fix
We chose **Solution 1 (Sync Send + Transaction)** because:
1.  It fixes the immediate data integrity risk.
2.  It requires minimal architectural changes.
3.  The latency cost is acceptable for this use case.
