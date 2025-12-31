# Apache Kafka Guide

## 1. What is Kafka?

Apache Kafka is a distributed **event streaming platform**. It is designed to handle high volumes of data in real-time, functioning as a highly durable and scalable "publish-subscribe" (pub/sub) messaging system.

**Project Example**  
In your `mastering-adv-ms` project, Kafka acts as the central nervous system connecting your services.
*   **Producers:**
    *   **Order Service:** Sends an update when an order is placed (`order-events`).
    *   **User Service:** Sends an update when a new user registers (`user-events`).
*   **Kafka:** Receives these updates and stores them safely in topics.
*   **Consumer:**
    *   **Notification Service:** Subscribes to both topics. When it sees a new order or user, it sends out an email or notification.

**How it works**  
1.  **Source:** A user buys a product (Order Service).
2.  **Ingestion:** `Order Service` sends a simplified message to Kafka.
3.  **Storage:** Kafka stores the event in the `order-events` Topic.
4.  **Consumption:** `Notification Service` picks up the message and triggers an alert.

This decouples your services. The **Order Service** just shouts "Order Created!" and moves on, without waiting for the Notification Service to confirm delivery.

---

## 2. Why Do We Use It?

In a microservices architecture like yours, we use Kafka primarily for **Decoupling** and **Resilience**.

### 2.1 Decoupling Services
In a traditional monolithic approach, `order-service` might directly call `notification-service`.
*   **Problem**: If `notification-service` is down (e.g., email server maintenance), `order-service` might fail or hang, preventing the user from checking out.
*   **Kafka Solution**: `order-service` sends a message to Kafka ("Order Created") and immediately returns success to the user. It doesn't care if `notification-service` is online. `notification-service` will process the message whenever it is ready.

### 2.2 Asynchronous Processing (Fire and Forget)
Your user doesn't have to wait for the email to be sent before seeing "Order Successful".
*   User clicks "Buy" -> Order Service saves to DB -> Pushes to Kafka -> Returns "Success".
*   The email sending happens in the background, independently.

### 2.3 Handling Backpressure & Traffic Bursts
If you get 1,000 user registrations in a second (e.g., a marketing campaign), your `notification-service` might crash if it tries to send 1,000 emails instantly.
*   **Kafka Solution**: Kafka acts as a **buffer**. It can absorb the 1,000 events instantly. Your `notification-service` can consume them at its own safe speed (e.g., 50 per second) without being overwhelmed.

---

## 3. Core Concepts

### 3.1 Topics & Partitions

A **Topic** is a category or feed name where records are stored. It is the logical container for messages (similar to a SQL Table or a File Folder).

In your project, you have two main topics defined in `KafkaConfig.java`:
*   **`order-events`**: For order-related activities.
*   **`user-events`**: For user registration activities.

To handle massive load, topics are split into **Partitions**.
*   **Partitioning:** Data can be spread across servers so multiple consumers can read in parallel.

### 3.2 Producers & Consumers

A **Producer** is the application that creates and publishes messages. A **Consumer** reads them.

*   **Producer:** `Order Service` & `User Service`.
    *   Code: `kafkaTemplate.send("order-events", message);`
*   **Consumer:** `Notification Service`.
    *   Code: `@KafkaListener(topics = "order-events", groupId = "notification-group")`

#### Internal Mechanics
*   **Batching:** Producers wait briefly to send messages in groups (batches) for better performance.
*   **Serialization:** Messages are converted to bytes (StringSerializer in your case) before sending.

### 3.3 Consumer Groups (Load Balancing)

A **Consumer Group** allows you to scale up message processing. Use `groupId` in your `@KafkaListener`.

*   **Current Setup:** `groupId = "notification-group"`
*   **Scaling:** If you launch 3 instances of `Notification Service` with the *same* group ID, Kafka will divide the work. Instance A handles some orders, Instance B handles others. This is automatic load balancing.
*   **Broadcasting:** If you added an `Analytics Service` aimed at tracking sales, you would give it a *different* group ID (e.g., `analytics-group`). Kafka would then send a copy of every message to both Notification and Analytics.

### 3.4 Offsets (The Bookmark)

How does Kafka know what `Notification Service` has already processed? It uses an **Offset**.

*   **Commit:** When `Notification Service` processes a message, it "commits" the offset (marks it as read).
*   **Resume:** If the service crashes and restarts, it checks the last committed offset and resumes from there. **No data is lost.**

---

## 4. Infrastructure Components

### 4.1 Broker

A **Broker** is a single Kafka server. It receives, stores, and serves data.
*   You typically run one broker locally (`localhost:9092`), but production systems have hundreds.
*   **Dumb Storage:** Brokers just store files. They don't know who read what. The *Consumers* track their own progress (offsets).

### 4.2 Cluster & Zookeeper

*   **Cluster:** A group of brokers working together.
*   **Zookeeper:** The manager. It keeps track of which brokers are alive and which broker is the "Leader" for a partition.
    *   *Vital:* You must start Zookeeper *before* starting Kafka.

---

## 5. Operations & Monitoring

### 5.1 How to Start (The Sequence Matters)

You strictly need to start Zookeeper first, then Kafka.

**Terminal 1: Start Zookeeper**
```cmd
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
```

**Terminal 2: Start Kafka (wait for Zookeeper to start first)**
```cmd
bin\windows\kafka-server-start.bat config\server.properties
```

### 5.2 Analysis & Monitoring Tools

Use these commands from your Kafka installation folder to check your specific project topics.

#### A. CLI Tools (Built-in)

*   **List Topics** (Should show `order-events` and `user-events`)
    ```cmd
    bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
    ```

*   **Run Console Producer** (Manually send a fake order)
    ```cmd
    bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic order-events
    ```
    *   *Type `Order #999` and hit Enter.*

*   **Run Console Consumer** (Watch for orders)
    ```cmd
    bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-events --from-beginning
    ```

*   **Check Consumer Group Lag** (See if Notification Service is falling behind)
    ```cmd
    bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group notification-group
    ```

#### B. GUI Tools (Recommended)
1.  **Offset Explorer:** Great for viewing raw message content.
2.  **Conduktor:** Good visual interface for dev.
3.  **Kafdrop:** Web UI.


