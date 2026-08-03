# AWS Lambda S3 Triggers & Invocation Guide

This document summarizes the architecture, programming models, and invocation patterns for **AWS Lambda S3 Triggers** using Java and Spring Boot.

---

## 📍 1. S3 Automatic Triggers vs. Programmatic SDK Invocation

In AWS Lambda, there are two distinct ways a function can be invoked:

### A. Automatic Event-Driven Trigger (S3 Event Notifications)
* **How it works**: Amazon S3 automatically detects file operations (`s3:ObjectCreated:*`) and invokes your Lambda function asynchronously.
* **When to use**: When you want automatic background processing upon file upload.
* **Code required**: No invocation code (`LambdaClient`) needed. S3 manages the trigger.

### B. Programmatic Invocation (`LambdaClient` & `InvokeRequest`)
* **How it works**: An external Java application (e.g. a Spring Boot REST API running on EC2/ECS) or another Lambda function explicitly triggers Lambda via AWS SDK v2.
* **When to use**: Service-to-service communication, function chaining, or manual background job triggering.

#### AWS SDK v2 Invocation Types:
| Invocation Type | Execution Mode | Description |
| :--- | :--- | :--- |
| **`RequestResponse`** | **Synchronous** | Caller blocks and waits for Lambda output. |
| **`Event`** | **Asynchronous** | Fire-and-forget; caller continues immediately. |
| **`DryRun`** | **Validation** | Verifies execution permissions without running code. |

```java
// Example: Programmatic Invocation using AWS SDK v2
LambdaClient lambdaClient = LambdaClient.create();

InvokeRequest invokeRequest = InvokeRequest.builder()
        .functionName("SpringS3TriggerFunction")
        .invocationType(InvocationType.REQUEST_RESPONSE)
        .payload(SdkBytes.fromUtf8String("{\"fileKey\": \"reports/data.csv\"}"))
        .build();

InvokeResponse response = lambdaClient.invoke(invokeRequest);
```

---

## 🛠 2. How to Write S3-Triggered Lambda Code in Java

When building an S3-triggered AWS Lambda in Java, you process the **`S3EventNotification`** event model.

### Approach 1: Spring Cloud Function (Used in this repository)

Uses Spring Boot dependency injection and exposes a `@Bean` returning a `Function<S3EventNotification, String>`.

#### 1. Define the Function Bean
```java
@Configuration
public class S3EventFunction {

    @Autowired
    private S3ProcessorService s3ProcessorService;

    @Bean
    public Function<S3EventNotification, String> s3EventConsumer() {
        return event -> {
            s3ProcessorService.processS3Event(event);
            return "SUCCESS";
        };
    }
}
```

#### 2. Extract Event Details & Process via AWS SDK v2 (`S3Client`)
```java
@Service
@RequiredArgsConstructor
public class S3ProcessorService {

    private final S3Client s3Client;

    public void processS3Event(S3EventNotification s3Event) {
        for (S3EventNotification.S3EventNotificationRecord record : s3Event.getRecords()) {
            String bucketName = record.getS3().getBucket().getName();
            String objectKey = URLDecoder.decode(record.getS3().getObject().getKey(), StandardCharsets.UTF_8);
            Long objectSize = record.getS3().getObject().getSizeAsLong();

            // Perform business logic (e.g. read object or metadata)
            HeadObjectResponse headObject = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
        }
    }
}
```

#### 3. SAM Template Handler (`template.yaml`)
```yaml
Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker
```

---

### Approach 2: Native AWS Lambda `RequestHandler` (Plain Java)

Standard Java handler implementation without Spring Framework overhead.

#### 1. Java Handler Implementation
```java
package com.example.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;

public class S3EventHandler implements RequestHandler<S3EventNotification, String> {

    @Override
    public String handleRequest(S3EventNotification event, Context context) {
        for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
            String bucket = record.getS3().getBucket().getName();
            String key = record.getS3().getObject().getKey();

            context.getLogger().log("Processed file: " + key + " from bucket: " + bucket);
        }
        return "SUCCESS";
    }
}
```

#### 2. SAM Template Handler (`template.yaml`)
```yaml
Handler: com.example.lambda.S3EventHandler::handleRequest
```

---

## 📊 Quick Summary Checklist

- [x] **Event Payload**: Use `S3EventNotification` to receive bucket & object details.
- [x] **Invocation**: S3 handles function invocation automatically on file upload.
- [x] **Spring Cloud Function**: Use `FunctionInvoker` as the SAM handler.
- [x] **AWS SDK v2**: Use `S3Client` to interact with S3 resources.
