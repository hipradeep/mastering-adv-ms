# Spring Boot AWS Lambda - Automated S3 Triggers

A production-grade **Spring Boot 3** project powered by **Spring Cloud Function AWS** and **AWS SDK v2** for processing **Automated S3 Trigger Events** in AWS Lambda.

---

## 📐 Architecture

```
┌────────────────────────┐      Object Uploaded     ┌────────────────────────────────────────────────────────┐
│   Source S3 Bucket     │ ──(s3:ObjectCreated:*)─> │  AWS Lambda Function (java17)                          │
│   (File Uploads)       │                          │  Handler: FunctionInvoker                              │
└────────────────────────┘                          │  Spring Bean: s3EventConsumer                          │
                                                    │  Service: S3ProcessorService                           │
                                                    └───────────────────────────┬────────────────────────────┘
                                                                                │ Writes Processing Result
                                                                                ▼
                                                    ┌────────────────────────────────────────────────────────┐
                                                    │   Destination S3 Bucket                                │
                                                    │   (Processed Summaries & Output)                       │
                                                    └────────────────────────────────────────────────────────┘
```

---

## 🛠 Tech Stack

* **Java 17**
* **Spring Boot 3.2.3**
* **Spring Cloud Function AWS 4.1.0**
* **AWS SDK v2 (`software.amazon.awssdk:s3`)**
* **AWS SAM (Serverless Application Model)**

---

## 📁 Project Structure

```
├── pom.xml                                      # Maven dependencies & build plugins
├── template.yaml                                # AWS SAM CloudFormation template
├── README.md                                    # Instructions & documentation
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/s3trigger/
    │   │       ├── S3TriggerApplication.java    # Spring Boot Main Class
    │   │       ├── config/
    │   │       │   └── AwsConfig.java           # S3Client Bean configuration
    │   │       ├── function/
    │   │       │   └── S3EventFunction.java     # Spring Cloud Function (s3EventConsumer)
    │   │       └── service/
    │   │           └── S3ProcessorService.java  # S3 File processing business logic
    │   └── resources/
    │       └── application.yml                  # Spring application settings
    └── test/
        ├── java/
        │   └── com/example/s3trigger/
        │       └── S3EventFunctionTest.java    # JUnit 5 & Mockito test
        └── resources/
            └── s3-event.json                    # Mock S3 Event payload
```

---

## ⚙️ How Spring Cloud Function AWS Handles S3 Events

1. **Lambda Handler Configuration**: In `template.yaml`, the Lambda handler is set to:
   ```
   org.springframework.cloud.function.adapter.aws.FunctionInvoker
   ```
2. **Bean Discovery**: Spring Cloud Function automatically discovers the `@Bean` named `s3EventConsumer` of type `Function<S3EventNotification, String>`.
3. **Payload Deserialization**: AWS S3 event notifications (JSON) are automatically deserialized into Java objects (`com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification`).
4. **Processing**: `S3ProcessorService` processes the S3 event using AWS SDK v2 (`S3Client`) to read file metadata or content and store outputs to a destination bucket.

---

## 🧪 Local Testing & Building

### 1. Run Unit Tests & Build Maven Package
```bash
mvn clean test
```

### 2. Build Uber-JAR for AWS Lambda
```bash
mvn clean package
```
This produces an executable Uber-JAR target: `target/s3-trigger-service-1.0.0-SNAPSHOT.jar`.

### 3. Local Test with SAM CLI (Optional)
```bash
sam local invoke SpringS3TriggerFunction -e src/test/resources/s3-event.json
```

---

## 🚀 AWS Deployment Guide

```bash
# 1. Build SAM Application
sam build

# 2. Deploy to AWS using Guided SAM CLI
sam deploy --guided
```

When prompted:
- **Stack Name**: `spring-s3-trigger-stack`
- **AWS Region**: `us-east-1` (or your preferred region)
- **Confirm IAM roles**: `y`
