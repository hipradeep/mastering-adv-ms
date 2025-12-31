# Apache Kafka Guide

## 1. What is Kafka?

Apache Kafka is a distributed **event streaming platform**. It is designed to handle high volumes of data in real-time, functioning as a highly durable and scalable "publish-subscribe" (pub/sub) messaging system.

**Example**  
Imagine a sports website sending live score updates.
*   **Producer:** The scoring system at the stadium sends an update: `Goal: Team A, 1-0`.
*   **Kafka:** Receives this update and stores it safely.
*   **Consumers:** The mobile app, the website, and the betting analytics engine all subscribe to Kafka. They all receive the `Goal` update instantly to update their displays.

**How it works**
1.  **Source:** An event occurs (Order Placed, Button Clicked).
2.  **Ingestion:** The event is sent to Kafka.
3.  **Storage:** Kafka stores the event in a Topic (like a folder).
4.  **Consumption:** Downstream services subscribe to the Topic and react to the event.

In your `mastering-adv-ms` project, this decouples the **Order Service** from the **Payment Service**. The Order Service just shouts "Order Created!" and moves on, without waiting for the Payment Service to pick up the phone.

---

## 2. Why Do We Use It?

In a microservices architecture like yours, we use Kafka primarily for **Decoupling** and **Resilience**.

### 2.1 Decoupling Services
In a traditional REST API (Monolithic) approach, `order-service` would directly call `payment-service`.
*   **Problem**: If `payment-service` is down, `order-service` fails. They are tightly coupled.
*   **Kafka Solution**: `order-service` just sends a message to Kafka ("Here is a new order") and moves on. It doesn't care if `payment-service` is online or offline. `payment-service` can pick it up whenever it's ready.

### 2.2 Asynchronous Processing (Fire and Forget)
Your user doesn't have to wait for the payment, inventory, and notification to finish before seeing "Order Successful".
*   You accept the order -> Push to Kafka -> Return "Success" to user immediately.
*   Background services handle the rest at their own pace.

### 2.3 Handling Backpressure & Traffic Bursts
If you get 1,000 orders in a second (e.g., Black Friday), your `payment-service` might crash if it receives 1,000 HTTP requests instantly.
*   **Kafka Solution**: Kafka acts as a **buffer**. It can absorb millions of messages. Your `payment-service` can consume them at its own safe speed (e.g., 50 per second) without being overwhelmed.

### 2.4 The Saga Pattern (Distributed Transactions)
Since you are using microservices, you can't have a single database transaction across services.
*   Kafka facilitates the **Saga Pattern**:
    1.  Order Service talks to Kafka: "Order Created"
    2.  Payment Service hears it -> Processes Payment -> Talks to Kafka: "Payment Completed"
    3.  Inventory Service hears it -> Reserves Stock.
    4.  If anything fails, a "Compensation Event" (e.g., "Payment Failed") is sent to Kafka to undo previous steps.

---

## 3. Core Concepts

### 3.1 Topics & Partitions

A **Topic** is a category or feed name where records are stored. It is the logical container for messages (similar to a SQL Table or a File Folder). To handle massive load, a single Topic is split into multiple logs called **Partitions**. These partitions can be spread across different servers.

*   **Topic:** `order-events`
*   **Partitions:** `Partition 0`, `Partition 1`
*   **Data:** Orders ending in odd numbers go to `Partition 0`; even numbers go to `Partition 1`.

Kafka guarantees order **only within a partition**. Queries are handled in parallel across partitions, which is how Kafka achieves its immense speed.

#### Internal Mechanics (The Log)
Internally, each partition is stored on the disk as a series of **Segment Use Files** (e.g., `00000.log`, `00000.index`).
*   **Sequential Writes**: Kafka appends new messages to the end of the current log file. This sequential access pattern is what makes Kafka faster than random-access databases.
*   **Retention**: Old segments are deleted based on time (e.g., 7 days) or size (e.g., 1GB), keeping the storage manageable.

### 3.2 Producers & Consumers

A **Producer** is the application that creates and publishes messages to a Kafka Topic. A **Consumer** is the application that subscribes to a Topic and processes the messages.

*   **Producer:** `Order Service`. It pushes a JSON payload `{ "orderId": 101, "amount": 50 }`.
*   **Consumer:** `Payment Service` and `Inventory Service`. Both listen for this message to perform their respective tasks (deduct money, reserve stock).

#### Internal Mechanics
*   **Batching & Compression:** The Producer doesn't send messages one-by-one. It waits (for milliseconds) to accumulate a "batch" of messages and compresses them (gzip, snappy) before sending. This drastically reduces network overhead.
*   **Partitioning Strategy:** The Producer decides which partition to send to. If you provide a **Key** (e.g., `OrderId`), it hashes the key so all events for Order #101 go to the same partition (guaranteeing order). If no key is provided, it Round-Robins across partitions.

### 3.3 Consumer Groups (Load Balancing)

A **Consumer Group** allows you to scale up your processing and ensure messages are distributed correctly.

*   **Broadcasting:** If you want both `Payment` and `Inventory` to get a message, put them in **different** groups. Kafka sends a copy to each group.
*   **Scaling Work:** If `Payment Service` is too slow, launch 3 instances of it and put them in the **same** group. Kafka will split the topics partitions among them. Instance A takes Partition 0, Instance B takes Partition 1. This ensures parallel processing without duplicate work.

### 3.4 Offsets (The Bookmark)

How does Kafka know what you have read? It uses an **Offset**. An offset is a simple integer ID (0, 1, 2...) assigned to every message in a partition.

*   **Commit:** When `Payment Service` finishes Order #5, it "commits" offset 5.
*   **Resume:** If the service crashes and restarts, it asks Kafka "Where was I?", and Kafka replies "You were at offset 5". The service resumes from #6. **No data is lost.**

---

## 4. Infrastructure Components

### 4.1 Broker

A **Broker** is a single Kafka server (node). It is the workhorse that receives, stores, and serves data.

#### What does it actually do?
*   **Receives Data:** Accepts messages from Producers.
*   **Stores Data:** Writes messages to its hard drive as log files.
*   **Serves Data:** Sends messages to Consumers when requested.

#### Key Characteristics
*   **Identification:** Every broker has a unique integer ID (e.g., `broker.id=1` in `server.properties`).
*   **Bootstrap Server:** You only need to know the address of **one** broker (e.g., `localhost:9092`). Once connected, it provides metadata about the entire cluster.
*   **Dumb Storage, Smart Client:** The Broker is "dumb"—it doesn't track what you've read. It just appends 0s and 1s to a file. The **Client** (Consumer) is "smart" and tracks its own offset. This design makes the Broker incredibly fast.

### 4.2 Cluster

A **Cluster** is a group of Brokers working together as a single system.

#### How it Protects Your Data (Replication)
The main superpower of a cluster is **Replication**. You don't just store data once; you store copies.
*   **Leader:** Broker 1 holds the "Leader" copy. All reads/writes happen here.
*   **Followers:** Broker 2 and 3 hold "Follower" copies, constantly syncing data from the Leader.

#### What Happens if a Server Crashes? (Fault Tolerance)
1.  **Crash:** Broker 1 (Leader) fails.
2.  **Detection:** The cluster notices the failure.
3.  **Election:** Broker 2 (a Follower) is instantly promoted to **Leader**.
4.  **Recovery:** Services automatically switch to Broker 2. **No data is lost.**

### 4.3 Zookeeper

**Zookeeper** is the centralized coordinator for the Kafka cluster. It manages the metadata, configuration, and state of the cluster.

Think of Zookeeper as the **Office Manager**. It keeps the attendance sheet (Which Brokers are alive?) and assigns desks (Which Broker owns Partition 0?).

Kafka Brokers are stateless; they rely on Zookeeper to tell them their role.
*   **Health Checks:** Zookeeper sends heartbeats to Brokers. If a heartbeat fails, Zookeeper removes that Broker from the cluster.
*   **Controller Election:** Zookeeper decides which Broker acts as the "Controller" to manage other brokers.
*   *Requirement:* You **must** start Zookeeper before starting the Kafka Brokers (as seen in your `run_kafka.bat` script).

#### Internal Mechanics
*   **Ephemeral Nodes:** Brokers create "ephemeral" files in Zookeeper. If the Broker disconnects, its session dies, and the ephemeral file disappears instantly. This is how Zookeeper "knows" a broker is dead.

---

## 5. Operations & Monitoring

### 5.1 How to Start (The Sequence Matters)

You cannot start Kafka without Zookeeper. The strict order is:
1.  **Start Zookeeper:** Wait for it to listen on port 2181.
2.  **Start Broker:** It will connect to Zookeeper on startup.

**Windows Commands:**
Run these in separate terminal windows from your Kafka installation folder:

**Terminal 1: Start Zookeeper**
```cmd
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

**Terminal 2: Start Kafka (wait for Zookeeper to start first)**
```cmd
bin\windows\kafka-server-start.bat config\server.properties
```

### 5.2 Analysis & Monitoring Tools

How do you see what's inside a topic or check if your consumers are lagging?

#### A. CLI Tools (Built-in)
Kafka comes with command-line scripts in `bin\windows`.

*   **List Topics**
    ```cmd
    bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
    ```

*   **Run Kafka Producer** (Type messages and hit Enter to send)
    ```cmd
    bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic order-events
    ```

*   **Run Kafka Consumer / Read only NEW messages**
    ```cmd
    bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-events
    ```

*   **Read ALL messages (from beginning)**
    ```cmd
    bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-events --from-beginning
    ```

*   **Describe Groups (Check Lag)**
    ```cmd
    bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --all-groups
    ```

#### B. GUI Tools (Recommended)
Visualizing topics is much easier with a GUI.
1.  **Offset Explorer (formerly Kafka Tool):** A simple desktop app for Windows. Great for quickly browsing messages as JSON.
2.  **Conduktor:** A powerful desktop client (free for dev) that shows visual graphs of partitions, consumer lag, and broker health.
3.  **Kafdrop / UI for Apache Kafka:** Web-based UIs that you can run as a Docker container.

