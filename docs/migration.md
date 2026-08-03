# Migration Guide: Moving Processing Metadata Storage from S3 to Amazon RDS

This document provides a comprehensive step-by-step guide for migrating the metadata/audit logging storage mechanism in the **Spring Boot AWS Lambda S3 Trigger** project from an **S3 Destination Bucket** to an **Amazon RDS (Relational Database)** instance (PostgreSQL/MySQL).

---

## 🏛 1. Architecture Transition

### Previous Architecture (S3 Output Storage)
```
┌────────────────────────┐      File Upload      ┌─────────────────────────────────────────┐      PutObject JSON      ┌────────────────────────┐
│   Source S3 Bucket     │ ──(s3:ObjectCreated)─>│ Lambda (Spring Boot 3 + AWS SDK v2)     │ ────────────────────────> │ Destination S3 Bucket  │
└────────────────────────┘                       └─────────────────────────────────────────┘                          └────────────────────────┘
```

### New Architecture (Amazon RDS Storage)
```
┌────────────────────────┐      File Upload      ┌─────────────────────────────────────────┐     JDBC Connection      ┌────────────────────────┐
│   Source S3 Bucket     │ ──(s3:ObjectCreated)─>│ Lambda (Spring Boot 3 + Spring Data JPA)│ ───(via RDS Proxy)────>  │   Amazon RDS Instance  │
└────────────────────────┘                       └─────────────────────────────────────────┘                          └────────────────────────┘
```

---

## 🛠 2. Step-by-Step Migration Implementation

### Step 1: Database Schema Creation

Execute the following DDL script on your Amazon RDS PostgreSQL/MySQL database:

```sql
CREATE TABLE file_processing_logs (
    id BIGSERIAL PRIMARY KEY,
    source_bucket VARCHAR(255) NOT NULL,
    source_key VARCHAR(1024) NOT NULL,
    size_bytes BIGINT NOT NULL,
    content_type VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_file_logs_bucket_key ON file_processing_logs(source_bucket, source_key);
CREATE INDEX idx_file_logs_status ON file_processing_logs(status);
```

---

### Step 2: Update `pom.xml` Dependencies

Add **Spring Data JPA** and the **PostgreSQL JDBC Driver** to `pom.xml`:

```xml
<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### Step 3: Configure Database Settings in `application.yml`

Update `src/main/resources/application.yml` to specify database connection parameters and connection pooling settings:

```yaml
spring:
  cloud:
    function:
      definition: s3EventConsumer
  datasource:
    url: ${DB_URL:jdbc:postgresql://rds-proxy.endpoint.amazonaws.com:5432/filedb}
    username: ${DB_USERNAME:dbuser}
    password: ${DB_PASSWORD:secretpassword}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 2
      minimum-idle: 1
      connection-timeout: 5000
      idle-timeout: 300000
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
    show-sql: false

aws:
  s3:
    region: us-east-1
```

> ⚠️ **Note**: In a serverless Lambda context, keep `maximum-pool-size` low (e.g. 2-5 connections per Lambda instance) to avoid exhausting database connections across scaled instances.

---

### Step 4: Add Java Entity & Repository

#### Create Entity: `FileProcessingLog.java`
`src/main/java/com/example/s3trigger/entity/FileProcessingLog.java`

```java
package com.example.s3trigger.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "file_processing_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileProcessingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_bucket", nullable = false)
    private String sourceBucket;

    @Column(name = "source_key", length = 1024, nullable = false)
    private String sourceKey;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
```

#### Create Repository: `FileProcessingLogRepository.java`
`src/main/java/com/example/s3trigger/repository/FileProcessingLogRepository.java`

```java
package com.example.s3trigger.repository;

import com.example.s3trigger.entity.FileProcessingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileProcessingLogRepository extends JpaRepository<FileProcessingLog, Long> {
}
```

---

### Step 5: Update `S3ProcessorService.java`

Modify `src/main/java/com/example/s3trigger/service/S3ProcessorService.java` to inject `FileProcessingLogRepository` and save logs directly to RDS:

```java
package com.example.s3trigger.service;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.example.s3trigger.entity.FileProcessingLog;
import com.example.s3trigger.repository.FileProcessingLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ProcessorService {

    private final S3Client s3Client;
    private final FileProcessingLogRepository logRepository;

    public void processS3Event(S3EventNotification s3EventNotification) {
        log.info("=== Spring Cloud Function S3 Event Processing Started ===");

        if (s3EventNotification == null || s3EventNotification.getRecords() == null) {
            log.warn("Received empty or null S3 event notification records.");
            return;
        }

        for (S3EventNotification.S3EventNotificationRecord record : s3EventNotification.getRecords()) {
            String eventName = record.getEventName();
            String bucketName = record.getS3().getBucket().getName();
            String objectKey = URLDecoder.decode(record.getS3().getObject().getKey(), StandardCharsets.UTF_8);
            Long objectSize = record.getS3().getObject().getSizeAsLong();

            try {
                // Fetch Content-Type from S3 HeadObject
                HeadObjectResponse headResponse = s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build());
                String contentType = (headResponse != null && headResponse.contentType() != null) 
                        ? headResponse.contentType() : "application/octet-stream";

                // Save processing audit log to RDS
                FileProcessingLog processingLog = FileProcessingLog.builder()
                        .sourceBucket(bucketName)
                        .sourceKey(objectKey)
                        .sizeBytes(objectSize)
                        .contentType(contentType)
                        .status("PROCESSED_SUCCESSFULLY")
                        .processedAt(Instant.now())
                        .build();

                FileProcessingLog savedLog = logRepository.save(processingLog);
                log.info("Saved processing audit log to RDS with ID: {}", savedLog.getId());

            } catch (Exception e) {
                log.error("Failed to process S3 object [{}] from bucket [{}]: {}", objectKey, bucketName, e.getMessage(), e);
                throw new RuntimeException("S3 Event processing failure", e);
            }
        }
    }
}
```

---

### Step 6: Update Infrastructure Template (`template.yaml`)

To connect Lambda to RDS, configure VPC subnets, security groups, and RDS Proxy in `template.yaml`:

```yaml
Resources:
  SpringS3TriggerFunction:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: .
      Handler: org.springframework.cloud.function.adapter.aws.FunctionInvoker
      Runtime: java17
      MemorySize: 1024
      Timeout: 30
      VpcConfig:
        SubnetIds:
          - subnet-xxxxxx1
          - subnet-xxxxxx2
        SecurityGroupIds:
          - sg-xxxxxx1
      Environment:
        Variables:
          DB_URL: jdbc:postgresql://rds-proxy-endpoint.amazonaws.com:5432/filedb
          DB_USERNAME: dbuser
          DB_PASSWORD: secretpassword
```

---

## ⚡ 3. Best Practices & Key Considerations for Serverless + RDS

1. **Always Use AWS RDS Proxy**:
   * AWS Lambda scales rapidly to thousands of concurrent executions. Direct connection pools can easily exhaust database connection limits.
   * **AWS RDS Proxy** manages connection pooling efficiently and multiplexes thousands of concurrent Lambda requests over a small pool of database connections.

2. **Mitigate Cold Start Latency**:
   * Hibernate/JPA entity scanning can add 1-2 seconds to Lambda cold starts.
   * If cold start speed is critical, consider using **Spring Data JDBC** or **Jdbi** instead of heavy ORM frameworks like Hibernate.

3. **Store Credentials Securely**:
   * Instead of plain text environment variables, use **AWS Secrets Manager** or **AWS Systems Manager Parameter Store** to inject database credentials at runtime.
