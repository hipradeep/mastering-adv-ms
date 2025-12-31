# Kafka Design & Scenario-Based Questions

## 1. How would you design Kafka for High Availability (HA)?
**Goal:** Ensure the system continues to function (read/write) even if one or more brokers (servers) crash.

**Architecture Strategy:**
1.  **Replication Factor:** Set `replication.factor=3`.
    *   This ensures every partition has 3 copies (1 Leader, 2 Followers).
    *   *Tolerance:* You can lose up to 2 brokers and still possess the data.
2.  **Min In-Sync Replicas:** Set `min.insync.replicas=2`.
    *   This forces the producer to wait for acknowledgment from at least 2 brokers before considering a write "safe".
    *   *Trade-off:* If 2 replicas are down, writes will fail (Availability vs. Consistency tradeoff).
3.  **Rack Awareness:**
    *   Distribute brokers across different physical racks or Availability Zones (AZs) in the cloud.
    *   *Config:* `broker.rack=us-east-1a`.
    *   Kafka will try to place replicas on different racks so a power outage in one rack doesn't kill all copies.
4.  **Unclean Leader Election:** Set `unclean.leader.election.enable=false`.
    *   Prevent a replica that is "out of sync" (missing new data) from becoming a leader. This prioritizes Data Consistency over Availability.

**Example:**
For a Banking Payment system, we run a 5-node cluster across 3 AWS Availability Zones. We set RF=3 and min.insync=2. If AZ-1 goes down, AZ-2 and AZ-3 continue to handle traffic with zero data loss.

---

## 2. How would you design Kafka for millions of users?
**Goal:** Handle massive throughput (Millions of Reads/Writes per second).

**Architecture Strategy:**
1.  **Partitioning (The Key to Scale):**
    *   Kafka scales horizontally by splitting a topic into partitions.
    *   *Math:* If one partition handles 10 MB/s, and you need 1 GB/s, you need at least 100 partitions.
2.  **Effective Keying Strategy:**
    *   Do **not** use random keys or valid null keys for high-scale implementation if ordering matters per user.
    *   Use `UserID` or `DeviceID` as the Partition Key. This ensures all data for User A goes to Partition 5, allowing ordered processing for that user.
3.  **Consumer Groups:**
    *   Deploy consumer microservices in a group. Scale the number of instances to match the number of partitions (e.g., 100 pods for 100 partitions).
4.  **Producer Tuning:**
    *   Use `linger.ms=20` and `batch.size=32KB` to group messages into larger batches. Fewer network calls = higher throughput.
5.  **Retention Policies:**
    *   Don't keep data forever. Use time-based (`7 days`) or log-compaction (keep only latest user state) to manage disk space.

**Example:**
Uber tracking millions of cars.
*   **Topic:** `driver-locations`
*   **Partitions:** 500.
*   **Key:** `DriverID`.
*   **Brokers:** 50 high-memory instances.

---

## 3. How would you design an event-driven microservice using Kafka?
**Scenario:** An E-Commerce system (Order -> Payment -> Inventory -> Notification).

**Design:**
1.  **De-coupling:** Services never call each other's REST APIs directly for core flows. They communicate via **Events**.
2.  **Topics:**
    *   `orders.created` (Produced by Order Service)
    *   `orders.paid` (Produced by Payment Service)
    *   `orders.fulfilled` (Produced by Inventory Service)
3.  **Flow:**
    *   **Order Service** places an order, saves it as "PENDING", and emits `OrderCreatedEvent`.
    *   **Payment Service** listens to `OrderCreatedEvent`. It charges the card.
        *   *Success:* Emits `OrderPaidEvent`.
        *   *Failure:* Emits `OrderPaymentFailedEvent`.
    *   **Inventory Service** listens to `OrderPaidEvent`. It allocates stock.
        *   *Success:* Emits `InventoryAllocatedEvent`.
    *   **Order Service** listens to `InventoryAllocatedEvent`. Updates status to "CONFIRMED".
4.  **Saga Pattern:** To handle rollbacks (e.g., Payment success but Inventory fail), listen to failure events and trigger compensating transactions (e.g., Refund Payment).

---

## 4. When should you NOT use Kafka?
Kafka is powerful, but not a silver bullet. Avoid it in these cases:

1.  **Real-Time Request/Response (RPC):**
    *   If the user is waiting for a response on the UI (e.g., "Search Products"), use REST or gRPC. Kafka is asynchronous; the delay is unpredictable.
2.  **Data with explicit filtering/querying needs:**
    *   Kafka is not a database. You cannot run `SELECT * FROM topic WHERE price > 100`. If you need complex querying, use a DB (Postgres/Elasticsearch).
3.  **Small Dataset / Low Complexity:**
    *   If you just have one service talking to another with low volume, Kafka is overkill. Use RabbitMQ or simple HTTP.
4.  **Large Processing of huge blobs:**
    *   Don't send 50MB videos via Kafka. Send a URL (S3 link) instead.

---

## 5. Kafka vs RabbitMQ?

| Feature | **Apache Kafka** | **RabbitMQ** |
| :--- | :--- | :--- |
| **Model** | **Log-based (Pull)**. Dumb broker, Smart consumer. Consumer tracks offset. | **Queue-based (Push)**. Smart broker. Broker tracks state. |
| **Persistence** | **Durable**. Stores data on disk for weeks/years. | **Transient**. Mostly in-memory. Deletes msg after ack. |
| **Throughput** | **Massive** (Millions/sec). | **Moderate** (Thousands/sec). |
| **Routing** | weak (basic partitioning). | **Rich** (Exchange, Bindings, Routing Keys, Topic wildcard). |
| **Replay** | **Yes**. Can rewind and re-read old data. | **No**. Once consumed, data is gone. |
| **Use Case** | Event Sourcing, Stream Processing, Big Data Pipelines. | Traditional Task Queues, Complex Routing needs. |

---

## 6. Kafka vs ActiveMQ?
*   **ActiveMQ (Classic/Artemis):** Similar to RabbitMQ (traditional JMS Message Broker).
*   **Architecture:** ActiveMQ is built on the JMS (Java Message Service) standard. Kafka is a complete departure from JMS.
*   **Comparison:**
    *   **Ordering:** Kafka guarantees ordering per partition. ActiveMQ has difficulties guaranteeing ordering in clustered setups.
    *   **Scale:** Kafka scales linearly by adding hardware. ActiveMQ has vertical scaling limits.
    *   **Legacy:** ActiveMQ is great for legacy Enterprise Java apps needing strict JMS compliance and XA Transactions. Kafka is for modern, scalable event streaming.

---

## 7. Kafka vs Redis Streams?
*   **Redis Streams:** A relatively new data structure in Redis (since v5.0) inspired by Kafka.
*   **Comparison:**
    *   **Latency:** Redis is **In-Memory**, so it has sub-millisecond latency (faster than Kafka).
    *   **Persistence:** Redis persistence (AOF/RDB) is less durable than Kafka's commit log.
    *   **Storage Limit:** Limited by RAM. Kafka is limited by Disk (cheaper and larger).
    *   **Use Case:** Redis Streams is great for ephemeral, lightweight, super-fast streaming where data loss is acceptable if RAM crashes (unless configured heavily). Kafka is for the reliable, persistent backbone of the enterprise.

---

## 8. How to ensure message ordering across services?
Kafka only guarantees ordering **within a Partition**. It does **not** guarantee global ordering across the whole topic.

**Design Strategy:**
1.  **Partition Key:** Always use a consistent Key (e.g., `OrderID` or `UserID`) when producing.
    *   *Result:* All events for `Order-123` (Created, Paid, Shipped) will land in *Partition 0*.
2.  **Single Thread per Partition:** Ensure your consumer processes a partition in a single thread (or ensures strict ordering if multi-threaded).
3.  **Global Ordering (The Anti-Pattern):**
    *   If you truly need global ordering (Message B must come after Message A for *all* customers), you force the topic to have **1 Partition**.
    *   *Result:* Your throughput is capped at the speed of 1 consumer. This kills scalability.
4.  **Sequence Numbers (Application Layer):**
    *   Include a sequence ID or timestamp in the payload.
    *   If Consumer receives Seq #4 but hasn't seen Seq #3, buffering it or rejecting it until #3 arrives (Complex to implement).

---

## 9. Advanced / Senior-Level

### 1. How does Kafka handle backpressure?
Backpressure (slowing down because you can't keep up) is handled naturally by Kafka's **Pull-based** architecture.
*   **Consumers:** The consumer pulls data at its own speed (`max.poll.records`). If it's slow, it just polls less frequently. The broker doesn't "push" data to crash the consumer.
*   **Producers:** If the broker is slow (disk I/O saturated), the producer's buffer (`buffer.memory`) fills up.
    *   Once full, the producer `.send()` method **blocks** (for `max.block.ms`).
    *   This forces the upstream application (e.g., REST API) to slow down, effectively propagating backpressure up the stack.

### 2. What is log compaction?
Log Compaction is a retention policy where Kafka deletes **old records** that have the same **Key**, keeping only the **latest state** for that key.
*   *Config:* `cleanup.policy=compact`
*   *Use Case:* User Profiles internally.
    *   Msg 1: `Key=UserA`, `Value={Email: old@test.com}`
    *   Msg 2: `Key=UserA`, `Value={Email: new@test.com}`
    *   *Result:* Kafka garbage collects Msg 1. The topic effectively becomes a "Key-Value Store".

### 3. Difference between log compaction and retention?
*   **Retention (`delete`):** "I keep messages for 7 days." (Time-based logic). Good for events like "User Clicked Button".
*   **Compaction (`compact`):** "I keep the *latest* message for every Key forever." (State-based logic). Good for "User Address" or "Account Balance".

### 4. What is transactional messaging?
It allows you to write to multiple topics (and consume from topics) atomically.
*   **Atomicity:** Either *all* messages are written to *all* topics, or *none* are.
*   **Mechanism:**
    *   Producer sends `beginTransaction()`, writes data, then `commitTransaction()`.
    *   A special **Transaction Coordinator** broker manages the state.
    *   It writes "Commit Markers" to the log. Consumers configured with `isolation.level=read_committed` will only see messages after the marker is confirmed.

### 5. How exactly-once works internally?
It combines **Idempotency** + **Transactions**.
1.  **Idempotent Producer:** Using `PID` (Producer ID) and `Sequence Numbers`, brokers deduplicate retries to ensures message A is written exactly once to the log.
2.  **Transactions (Atomic Commit):**
    *   Consumer reads offset X (Input).
    *   Producer writes message Y (Output).
    *   Producer writes commit marker for offset X (Commit).
    *   The "Transaction Coordinator" ensures these 3 steps happen atomically. If the process crashes, the transaction is aborted, and the consumer reads X again to produce Y again.

### 6. How Kafka handles split-brain scenario?
"Split Brain" is when two brokers think they are the Leader (or Controller).
*   **Controller Generation (Epoch):**
    *   ZK/KRaft assigns a strictly increasing integer (Epoch) to the Controller (e.g., Epoch 5).
    *   If the old controller (Epoch 4) wakes up from a deep GC freeze and tries to send a command, brokers reject it because `4 < 5`.
*   **Leader Epoch:**
    *   Similar logic applies to Partition Leaders. If a broker claims "I am Leader of Partition 0", other brokers check its Epoch.

### 7. What is KRaft mode?
**KRaft** (Kafka Raft) is the new consensus protocol that **removes the dependency on ZooKeeper**.
*   **Before:** Kafka used Zookeeper for storing metadata (Topics, Partitions, ACLs) and electing controllers.
*   **Now:** Kafka manages its own metadata using an internal Raft Quorum.
*   **Benefit:** Simplified architecture (one less system to manage), faster controller failover, and supports millions of partitions.

### 8. Zookeeper vs KRaft?
*   **Zookeeper:**
    *   External complex system.
    *   Metadata writes are slow (limit on partition count ~200k).
    *   Controller failover takes time (loading state from ZK).
*   **KRaft:**
    *   Internal algorithm (Raft).
    *   Metadata stored in a Kafka topic (`@metadata`).
    *   Controller failover is near-instant (hot standby controllers already have the data).

### 9. How to migrate Kafka clusters?
Migrating partitions to new brokers (scaling up) or replacing hardware.
*   **Tools:** `kafka-reassign-partitions.sh` or "Cruise Control" (LinkedIn tool).
*   **Process:**
    1.  Generate a JSON plan mapping partitions to new Broker IDs.
    2.  Execute the reassignment.
    3.  Kafka replicates data to the new brokers (Throttling is crucial here to avoid saturating the network).
    4.  Once synced, new brokers become leaders. Old brokers delete data.

### 10. How to handle schema evolution in production?
Schema changes are inevitable.
1.  **Schema Registry:** Always use it. Enforce compatibility rules (BACKWARD/FORWARD).
2.  **Separate Topics:** If a breaking change is needed (v2):
    *   Create `orders-v2`.
    *   Write a "Stream Processor" to translate `orders-v1` -> `orders-v2` in real-time.
    *   Point new consumers to `orders-v2`.
    *   Eventually decommission `orders-v1`.

