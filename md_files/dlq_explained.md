# Dead Letter Queues (DLQ) in Kafka

## How is a DLQ Created?

In Spring Boot with Kafka, a Dead Letter Queue (DLQ) is **not created automatically** by default. It requires specific configuration in your consumer factory.

### 1. The Concept
A DLQ is simply a normal Kafka topic (e.g., `inventory.commands.DLQ`) where messages are sent after they fail to be processed multiple times.

### 2. How to Configure (The "Creation")
To "create" the mechanism, you must configure an `ErrorHandler` (specifically `DefaultErrorHandler` in modern Spring Kafka) with a `DeadLetterPublishingRecoverer`.

**Example Configuration (`KafkaConsumerConfig.java`):**

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
        ConsumerFactory<String, Object> consumerFactory,
        KafkaTemplate<String, Object> kafkaTemplate) {

    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);

    // 1. Create the Recoverer (Sends to DLQ topic)
    // By default, it sends to <original-topic>.DLQ
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

    // 2. Create the Error Handler (Retries 3 times, then calls Recoverer)
    DefaultErrorHandler errorHandler = new DefaultErrorHandler(
            recoverer, 
            new FixedBackOff(1000L, 3)); // 1 sec interval, 3 attempts

    factory.setCommonErrorHandler(errorHandler);

    return factory;
}
```

### 3. The Physical Topic
The actual topic `inventory.commands.DLQ` must exist on the Kafka Broker.
-   **Auto-creation:** If your broker has `auto.create.topics.enable=true`, it will be created the first time the recoverer tries to send a message to it.
-   **Manual:** Otherwise, you must create it using the CLI or a `NewTopic` bean.

---

## Scenarios: When is a DLQ Generated?

In your **current codebase**, a DLQ will **NEVER** be generated.

### Why?
1.  **Swallowed Exceptions:** Your `InventoryKafkaConsumer.java` catches all exceptions (`catch (Exception e)`) and only logs them.
    -   Spring Kafka sees this as "successful processing".
    -   The offset is committed.
    -   The message is effectively **lost** (swallowed), not sent to a DLQ.
2.  **No Configuration:** `KafkaConsumerConfig.java` does not have an `ErrorHandler` or `DeadLetterPublishingRecoverer` configured.

### Scenarios (If Configured Correctly)
If you implement the configuration above and remove the `catch` block, the following scenarios will send messages to the DLQ:

1.  **Business Logic Errors:**
    -   Example: `inventoryService.updateStock` throws a `RuntimeException` (e.g., DB down, data integrity violation).
    -   The consumer retries 3 times.
    -   If it still fails, the message is moved to the DLQ.

2.  **Deserialization Failures (Poison Pills):**
    -   *Current State:* Your config uses `JsonDeserializer` directly. If a malformed JSON message arrives, the consumer will likely crash or loop indefinitely because it happens *before* your code runs.
    -   *With DLQ:* You need to use `ErrorHandlingDeserializer`. If deserialization fails, it passes a `null` value to the listener (or exception wrapper), allowing the `ErrorHandler` to send the original raw bytes to the DLQ.

3.  **Timeout/Network Issues:**
    -   Repeated timeouts when calling external APIs during message processing.
