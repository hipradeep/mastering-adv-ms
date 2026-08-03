# How Automated S3 Triggers Work with Spring Boot AWS Lambda

This document explains the end-to-end architecture, event lifecycle, and technical implementation of **Automated S3 Triggers** using **Spring Boot 3**, **Spring Cloud Function AWS**, and **AWS SDK v2**.

---

## 🏛 Architecture Diagram

```
┌────────────────────────┐      1. File Upload      ┌────────────────────────────────────────────────────────┐
│   Source S3 Bucket     │ ───────────────────────> │                   Amazon S3 Service                    │
│ (e.g. upload-bucket)   │                          │ - Emits s3:ObjectCreated:* event notification          │
└────────────────────────┘                          └───────────────────────────┬────────────────────────────┘
                                                                                │
                                                                                │ 2. Async Invocation
                                                                                ▼
┌────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                             AWS Lambda Environment                                         │
│                                                                                                            │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │ 3. Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker                           │  │
│  └───────────────────────────────────────────────────┬──────────────────────────────────────────────────┘  │
│                                                      │ Deserializes JSON -> S3EventNotification            │
│                                                      ▼                                                     │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │ 4. Spring Cloud Function: s3EventConsumer (Function<S3EventNotification, String>)                    │  │
│  └───────────────────────────────────────────────────┬──────────────────────────────────────────────────┘  │
│                                                      │ Delegates to Service                                │
│                                                      ▼                                                     │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │ 5. Service: S3ProcessorService                                                                       │  │
│  │    - Extracts Object Key & Bucket Name                                                               │  │
│  │    - Calls S3Client.headObject(...) via AWS SDK v2                                                   │  │
│  │    - Writes output summary record                                                                    │  │
│  └───────────────────────────────────────────────────┬──────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┼─────────────────────────────────────────────────────┘
                                                       │
                                                       │ 6. PutObject (Summary Output)
                                                       ▼
                                    ┌────────────────────────────────────┐
                                    │       Destination S3 Bucket        │
                                    │    (e.g. processed-bucket)         │
                                    └────────────────────────────────────┘
```

---

## 🔄 Step-by-Step Execution Lifecycle

### 1. File Upload & Event Notification
When a user or external service uploads a file to the configured **Source S3 Bucket**, Amazon S3 detects object creation events matching the `s3:ObjectCreated:*` event pattern (e.g. `Put`, `Post`, `Copy`, `CompleteMultipartUpload`).

Amazon S3 constructs a JSON event payload containing:
* **Bucket Metadata**: Bucket Name, Bucket ARN.
* **Object Metadata**: Object Key (file path), Object Size, ETag, Sequencer.
* **Event Details**: Event Name, Event Time, AWS Region, User Identity.

---

### 2. AWS Lambda Async Invocation
Amazon S3 invokes the **AWS Lambda Function** asynchronously:
* The Lambda service places the event into an internal AWS event queue.
* Lambda retries execution up to **2 times** automatically if the handler throws an unhandled exception.

---

### 3. Spring Cloud Function AWS Adapter (`FunctionInvoker`)
In `template.yaml`, the Lambda entry point handler is set to:
```yaml
Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker
```
`FunctionInvoker` performs the following core tasks:
1. Bootstraps the Spring context using `S3TriggerApplication.class`.
2. Locates the active Spring Cloud Function bean defined in `application.yml`:
   ```yaml
   spring:
     cloud:
       function:
         definition: s3EventConsumer
   ```
3. Deserializes the incoming AWS S3 JSON event into a strongly-typed Java object: `com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification`.

---

### 4. Spring Bean Processing (`S3EventFunction`)
The Spring bean `s3EventConsumer` of type `Function<S3EventNotification, String>` receives the deserialized event and delegates to `S3ProcessorService`:

```java
@Bean
public Function<S3EventNotification, String> s3EventConsumer() {
    return event -> {
        log.info("Received S3EventNotification in Spring Cloud Function...");
        s3ProcessorService.processS3Event(event);
        return "SUCCESS";
    };
}
```

---

### 5. Business Logic Execution (`S3ProcessorService`)
`S3ProcessorService` performs the following operations:
1. **URL Decoding**: Decodes the object key (e.g., converting URL-encoded characters like `+` to spaces).
2. **Metadata Inspection**: Calls `S3Client.headObject(...)` via AWS Java SDK v2 to inspect content type, headers, and metadata.
3. **Automated Processing**: Executes custom business logic (e.g., CSV/JSON parsing, image processing, or data ETL).
4. **Summary Output**: Constructs a processing summary JSON and writes it to the **Destination S3 Bucket** via `S3Client.putObject(...)`.

---

## 🛠 Infrastructure as Code (`template.yaml`)

The infrastructure setup configures the event trigger connection declaratively using AWS SAM:

```yaml
  SourceUploadBucket:
    Type: AWS::S3::Bucket

  SpringS3TriggerFunction:
    Type: AWS::Serverless::Function
    Properties:
      Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker
      Runtime: java17
      Events:
        S3UploadEvent:
          Type: S3
          Properties:
            Bucket: !Ref SourceUploadBucket
            Events: s3:ObjectCreated:*
```

---

## ⚡ Performance & Production Best Practices

1. **Preventing Event Loops (Infinite Loops)**:
   * **Rule**: Never write output files back to the **same** S3 bucket with the same event trigger without prefix/suffix filters.
   * **Solution**: Use separate buckets for upload (`SourceBucket`) and output (`ProcessedBucket`), or apply strict prefix rules (e.g. trigger only on `incoming/` prefix).

2. **Cold Start Optimization**:
   * Enabled JVM flags in `template.yaml`:
     ```yaml
     JAVA_TOOL_OPTIONS: "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
     ```
   * Decreases JVM startup time during initial Lambda initialization.

3. **Idempotency**:
   * Since S3 event notifications guarantee at-least-once delivery, `S3ProcessorService` uses deterministic output keys based on timestamps and file basenames to ensure safe re-runs.
