# Swallowed Kafka Errors (Fire-and-Forget Risk)

## The Problem
In Spring Boot's `KafkaTemplate`, the `send()` method is **asynchronous** by default. It returns a `CompletableFuture` (or `ListenableFuture` in older versions) and immediately returns control to the caller.

### The Code Pattern
```java
// Danger: Fire-and-Forget
kafkaTemplate.send(topic, key, message);
return "Success";
```

### The Consequence
1.  **False Success:** The application logs "Sending..." and returns `200 OK` to the user/frontend immediately.
2.  **Silent Failure:** If the Kafka Broker is down, the topic doesn't exist, or serialization fails, the error happens on a separate thread in the background.
3.  **Data Loss:** The user thinks the transaction is complete, but the message was never actually delivered to the queue. The system is now in an inconsistent state.

---

## The Fix: Synchronous Send

To ensure data integrity, we must wait for the broker to acknowledge the receipt of the message before proceeding.

### The Fixed Code
We modified `KafkaProducerService.java` to use `.get()`:

```java
try {
    // Block the thread until the Future completes
    kafkaTemplate.send(topic, key, message).get();
} catch (Exception e) {
    // If Kafka fails, throw RuntimeException to trigger DB Rollback
    throw new RuntimeException("Failed to send message to Kafka", e);
}
```

### Why this works
1.  **Guaranteed Delivery:** The method will not return until Kafka confirms storage (based on `acks` config).
2.  **Atomic Transaction:** Because this logic is inside a method annotated with `@Transactional`, throwing the `RuntimeException` forces the Database Transaction to rollback.
    -   Kafka Fail = DB Rollback.
    -   No "Zombie" records in the database.

## Trade-offs
-   **Latency:** The HTTP request will take slightly longer because it includes the network round-trip time to Kafka.
-   **Throughput:** Synchronous sends reduce maximum throughput compared to async, but provide the necessary consistency guarantees for financial/inventory transactions.
