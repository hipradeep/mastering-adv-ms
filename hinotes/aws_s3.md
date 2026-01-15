# AWS S3 (Simple Storage Service) Deep Dive

## 1. Core Concepts
*   **Object Storage**: Stores data as objects (files) within buckets.
*   **Bucket**: A container for objects.
    *   **Global Namespace**: Bucket names must be unique across *all* AWS accounts globally.
    *   **Region-Specific**: Buckets are created in a specific region, but S3 is a global service.
*   **Object**: Consists of Key (name), Value (data), Version ID, Metadata, and ACLs.

## 2. Storage Classes
*   **S3 Standard**: 99.99% availability, 11 9s durability. Frequent access.
*   **S3 Intelligent-Tiering**: Automatically moves data between tiers based on access patterns. No retrieval fees.
*   **S3 Standard-IA (Infrequent Access)**: Lower cost for storage, but retrieval fee applies. Rapid access when needed.
*   **S3 One Zone-IA**: Stored in a single AZ. Lower cost (20% less than Standard-IA). Risk of data loss if AZ fails.
*   **S3 Glacier**: Low-cost archiving. Retrieval times from minutes to hours.
*   **S3 Glacier Deep Archive**: Lowest cost. Retrieval time 12-48 hours.

## 3. Security
*   **Bucket Policies**: JSON-based policies applied to the bucket. Controls access for cross-account or public access.
*   **Access Control Lists (ACLs)**: Legacy method. Grant read/write access to other AWS accounts.
*   **Block Public Access**: A centralized setting to prevent accidental public exposure of data.
*   **Encryption**:
    *   **SSE-S3**: Keys managed by S3.
    *   **SSE-KMS**: Keys managed by AWS KMS (Key Management Service). Provides audit trails.
    *   **SSE-C**: Customer-provided keys.

## 4. Key Features
*   **Versioning**: Keep multiple variants of an object. Protects against accidental deletes/overwrites.
*   **Lifecycle Policies**: Automate transitioning objects to cheaper storage classes (e.g., move to Glacier after 30 days) or expiring them.
*   **Replication**:
    *   **CRR (Cross-Region Replication)**: Compliance, lower latency for global users.
    *   **SRR (Same-Region Replication)**: Log aggregation, prod/test sync.
*   **Presigned URLs**: Temporary access to private objects via a URL. Useful for allowing users to upload/download without AWS credentials.

## 5. Performance
*   **Multipart Upload**: Recommended for files > 100MB. Uploads parts in parallel.
*   **Prefix Randomization**: (Historical) Previously needed for high request rates. Now S3 scales automatically to 3,500 PUT/COPY/POST/DELETE per prefix per second.

## 6. Interview Tips
*   **Consistency Model**: S3 provides strong read-after-write consistency.
*   **"Access Denied"**: Check both IAM Policies (User) AND Bucket Policies. An explicit DENY in either blocks access.
*   **Cost Optimization**: Use Lifecycle policies to move old data to Glacier.

## 7. Spring Boot Integration
### Spring Cloud AWS
*   **Auto-configuration**: Simplifies S3 interaction. Simply add `spring-cloud-starter-aws-s3` dependency.
*   **Resource Access**: Load S3 objects as Spring Resources.
    ```java
    @Value("s3://my-bucket/my-file.txt")
    private Resource s3Resource;
    ```
*   **S3Template**: Higher-level abstraction for common operations (upload, download, signed URLs).

### Common Microservices Patterns
*   **Presigned URLs**:
    *   *Problem*: Don't upload huge files to your Spring Boot backend (it blocks threads and consumes memory).
    *   *Solution*: Generate a **Presigned URL** in Spring Boot, send it to the frontend (React/Angular), and let the browser upload directly to S3.
*   **Event Notifications**: Trigger a Lambda function or an SQS message when a file is uploaded to S3, which your Spring Boot app can then process asynchronously.

