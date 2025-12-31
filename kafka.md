# Apache Kafka Guide

## 1. What is Kafka?

Apache Kafka is a distributed **event streaming platform**. It is designed to handle high volumes of data in real-time, functioning as a highly durable and scalable "publish-subscribe" (pub/sub) messaging system.

**Project Example**  
In your `mastering-adv-ms` project, Kafka acts as the central nervous system connecting your services.
*   **Producers:**
    *   **Order Service:** Sends an update when an order is placed (`order-events`).
*   **Kafka:** Receives these updates and stores them safely in topics.
*   **Consumer:**
    *   **Payment Service:** Subscribes to `order-events`. When it sees a new order, it processes the payment.

**How it works**  
1.  **Source:** A user buys a product (Order Service).
2.  **Ingestion:** `Order Service` sends a simplified message to Kafka.
3.  **Storage:** Kafka stores the event in the `order-events` Topic.
4.  **Consumption:** `Payment Service` picks up the message and creates a Payment record.

This decouples your services. The **Order Service** just shouts "Order Created!" and moves on, without waiting for the Payment Service.

---

## 2. Why Do We Use It?

In a microservices architecture like yours, we use Kafka primarily for **Decoupling** and **Resilience**.

### 2.1 Decoupling Services
In a traditional monolithic approach, `order-service` might directly call `payment-service`.
*   **Problem**: If `payment-service` is down, `order-service` might fail.
*   **Kafka Solution**: `order-service` sends a message to Kafka ("Order Created") and immediately returns success. `payment-service` will process the message whenever it is ready.

### 2.2 Asynchronous Processing (Fire and Forget)
Your user doesn't have to wait for the payment logic to finish before seeing "Order Placed".

### 2.3 Handling Backpressure & Traffic Bursts
If you get 1,000 orders in a second, your `payment-service` might crash if it tries to process 1,000 requests instantly (e.g., hitting banking APIs).
*   **Kafka Solution**: Kafka acts as a **buffer**. It can absorb the 1,000 events instantly. Your `payment-service` can consume them at its own safe speed.

---

## 3. Core Concepts

### 3.1 Topics & Partitions

A **Topic** is a category or feed name where records are stored.
In your project, you have main topics:
*   **`order-events`**: For order-related activities.
*   **`payment-events`**: For payment-related activities.

### 3.2 Producers & Consumers

A **Producer** is the application that creates and publishes messages. A **Consumer** reads them.

*   **Producer:** `Order Service`.
    *   Code: `kafkaTemplate.send("order-events", message);`
*   **Consumer:** `Payment Service`.
    *   Code: `@KafkaListener(topics = "order-events", groupId = "payment-group")`

#### Internal Mechanics
*   **Batching:** Producers wait briefly to send messages in groups (batches) for better performance.
*   **Serialization:** Messages are converted to bytes.

### 3.3 Consumer Groups (Load Balancing)

A **Consumer Group** allows you to scale up message processing. Use `groupId` in your `@KafkaListener`.

*   **Current Setup:** `groupId = "payment-group"`
*   **Scaling:** If you launch 3 instances of `Payment Service` with the *same* group ID, Kafka will divide the work.

### 3.4 Offsets (The Bookmark)

How does Kafka know what `Payment Service` has already processed? It uses an **Offset**.

*   **Commit:** When `Payment Service` processes a message, it "commits" the offset.
*   **Resume:** If the service crashes and restarts, it checks the last committed offset and resumes from there.

---

## 4. Infrastructure Components

### 4.1 Broker

A **Broker** is a single Kafka server. It receives, stores, and serves data.

### 4.2 Cluster & Zookeeper

*   **Cluster:** A group of brokers working together.
*   **Zookeeper:** The manager. It keeps track of which brokers are alive.

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

*   **List Topics**
    ```cmd
    bin\windows\kafka-topics.bat --bootstrap-server localhost:9092 --list
    ```

*   **Run Console Producer**
    ```cmd
    bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic order-events
    ```

*   **Run Console Consumer**
    ```cmd
    bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-events --from-beginning
    ```

*   **Check Consumer Group Lag**
    ```cmd
    bin\windows\kafka-consumer-groups.bat --bootstrap-server localhost:9092 --describe --group payment-group
    ```



