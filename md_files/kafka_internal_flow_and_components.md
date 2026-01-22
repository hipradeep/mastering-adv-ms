# Apache Kafka: Internal Attributes, Flow, and Components

This document outlines the core architecture, components, and internal data flow of Apache Kafka.

## 1. Core Components

### 1.1 Broker
A single Kafka server. A Kafka cluster is composed of multiple brokers. Brokers receive messages from producers, store them on disk, and serve them to consumers. Use `bootstrap.servers` to connect.

### 1.2 Topic
A logical category or feed name to which records are published. Topics are:
-   **Immutable:** Once data is written, it cannot be changed.
-   **Partitioned:** Split into logs called partitions.

### 1.3 Partition
The atomic unit of storage and parallelism in Kafka.
-   Each partition is an ordered, immutable sequence of records.
-   **Leader:** One broker is the "Leader" for a partition. It handles all reads and writes.
-   **Follower:** Other brokers replicate the data for fault tolerance. They do not serve client requests (usually).

### 1.4 Segment
Partitions are physically split into **Segments** (`.log` files) on the disk.
-   Kafka writes to an "active" segment.
-   Old segments are deleted or compacted based on retention policies.

### 1.5 Offset
A unique integer ID assigned to every message within a partition. It marks the position of the message in the log.

### 1.6 Zookeeper / Controller (KRaft)
-   **Zookeeper (Legacy):** Managed cluster metadata (broker states, ACLs, configs) and leader election.
-   **KRaft (Modern):** Removes Zookeeper dependency. Metadata is stored in a special internal Kafka topic, managed by a Quorum Controller.

---

## 2. Internal Data Flow

### 2.1 The Write Path (Producer Flow)
1.  **Producer sends a record.**
2.  **Partitioner:** The producer client determines which partition to send to (Round-robin or based on Key hash).
3.  **Batching:** Records are accumulated in memory buffers per partition.
4.  **Send:** A batch is sent to the **Leader Broker** of that partition.
5.  **Write:** The Leader writes the batch to its local log (page cache -> disk).
6.  **Replication:** Follower brokers fetch the new data from the Leader.
7.  **Commit:** Once the message is replicated to all **ISR (In-Sync Replicas)**, the message is considered "committed."
8.  **Ack:** The Leader sends an acknowledgement back to the Producer (depending on `acks` setting).

### 2.2 The Read Path (Consumer Flow)
1.  **Subscribe:** Consumer subscribes to a topic.
2.  **Assignment:** The Group Coordinator (a broker) assigns specific partitions to the consumer.
3.  **Poll:** Consumer sends a `fetch` request to the Leader Broker.
4.  **Read:** Broker reads data from the filesystem (Zero-Copy optimization: sends data directly from OS cache to network socket).
5.  **Processing:** Consumer processes the records.
6.  **Commit Offset:** Consumer sends a request to the `__consumer_offsets` topic to save its progress.

---

## 3. Key Concepts

### Consumer Groups
A set of consumers working together to consume a topic.
-   Kafka ensures that **each partition is consumed by exactly one consumer** within the group.
-   This allows for massive horizontal scalability processing.

### ISR (In-Sync Replicas)
A subset of replicas that are "caught up" with the leader.
-   **High Watermark:** The offset of the last message successfully replicated to all ISRs. Consumers can only read up to this point.

### Rebalancing
When a consumer joins or leaves a group (or a broker fails), Kafka triggers a **Rebalance**.
-   Ownership of partitions is reassigned among the active members of the group.
-   Example: If Consumer A dies, its partitions are given to Consumer B.

## 4. Visualizing the Architecture

```mermaid
graph TD
    subgraph Cluster
        B1[Broker 1 (Controller)]
        B2[Broker 2]
        B3[Broker 3]
    end

    P[Producer] -->|Writes to Leader| B2
    B2 -- Replicates to --> B1
    B2 -- Replicates to --> B3

    C1[Consumer A] -->|Reads from| B2
    C2[Consumer B] -->|Reads from| B2

    subgraph "Topic: Orders"
        Part0[Partition 0 (Leader: B2)]
    end
```
