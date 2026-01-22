# How to Ensure FIFO (First-In-First-Out) Message Consumption in Kafka

Apache Kafka does **not** guarantee total ordering of messages across an entire topic. However, it **does** guarantee strict ordering of messages **within a specific partition**.

To achieve FIFO (First-In-First-Out) message consumption, you must design your producer and consumer specifically to leverage partition-level ordering.

## 1. The Golden Rule: Partitioning

Kafka guarantees that messages sent to the same partition are appended and consumed in the order they were sent.

**Requirement:** All messages that *must* be processed in order relative to each other must go to the **same partition**.

### How to achieve this:
*   **Use Keys:** When sending a message from the Producer, always specify a **Key**.
    *   Kafka uses the key to hash the message to a specific partition.
    *   *Example:* If you are processing orders, use the `orderId` as the key. This ensures all events for Order #123 (Created, Paid, Shipped) land in the same partition and are consumed in that exact sequence.
    *   *Without a key:* Kafka will distribute messages round-robin (or sticky) across partitions, breaking the order for related events.

## 2. Producer Configuration

Even with keys, network retries can reorder messages (e.g., Message A fails, Message B succeeds, then Message A retry succeeds -> Order becomes B, A).

To prevent this, use the following configurations:

### Option A: Idempotent Producer (Recommended)
This is the modern and easiest way to ensure ordering and exactly-once semantics per partition.
*   `enable.idempotence = true`
*   `max.in.flight.requests.per.connection` <= 5 (Kafka maintains order with this setting if idempotence is enabled).
*   `acks = all`
*   `retries > 0`

### Option B: Strict Ordering (Legacy/Non-Idempotent)
If you cannot use idempotence:
*   `max.in.flight.requests.per.connection = 1`
    *   *Why?* This ensures the producer waits for an acknowledgement of the current message before sending the next one. If Message A fails, it won't send Message B until Message A is successfully retried.
    *   *Trade-off:* Significantly reduces throughput.

## 3. Consumer Configuration

Ordering on the broker is useless if the consumer processes messages out of order.

*   **Single Consumer per Partition:** Kafka automatically assigns a partition to only **one** consumer instance within a Consumer Group. This ensures that one machine reads the ordered log of that partition.
*   **Sequential Processing:** Your consumer logic must process messages sequentially.
    *   *Do not* hand off messages to a thread pool for parallel processing *unless* that thread pool also maintains ordering (e.g., sharding by key). If you strictly process messages in the `poll()` loop, you are safe.

## summary Checklist

| Component | Setting/Action | Reason |
| :--- | :--- | :--- |
| **Topic** | Design choice | Ordering is only guaranteed per partition. |
| **Producer** | Send with a **Key** | Ensures related messages go to the same partition. |
| **Producer** | `enable.idempotence=true` | Prevents duplicates and maintains order during retries. |
| **Producer** | `max.in.flight.requests.per.connection` | Set to 1 (if strict legacy) or <=5 (if idempotent). |
| **Consumer** | Single Thread / Sharded Threads | Don't process messages from the same partition in parallel threads randomly. |

## Example Scenario: Order Processing

1.  User places an order (`OrderCreated`).
2.  User pays (`OrderPaid`).

If these go to different partitions, the consumer might receive `OrderPaid` before `OrderCreated` (impossible logic).

**Solution:**
Producer sends both messages with `key = userId` (or `orderId`).  
-> Both go to Partition 3.  
-> Consumer reads Partition 3 sequentially.  
-> Consumer processes `OrderCreated` first, then `OrderPaid`.
