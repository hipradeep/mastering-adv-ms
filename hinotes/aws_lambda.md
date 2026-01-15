# AWS Lambda (Serverless) Deep Dive

## 1. Core Concepts
*   **Serverless**: You don't manage servers. You upload code, and AWS handles provisioning, scaling, and patching.
*   **FaaS (Function-as-a-Service)**: Event-driven execution model.
*   **Stateless**: Each invocation is independent. Local file system (`/tmp`) is ephemeral (though preserved for reuse in warm starts).
*   **Limits**:
    *   **Timeout**: Max 15 minutes.
    *   **Memory**: 128 MB to 10 GB (CPU scales with memory).

## 2. Triggers (Event Sources)
Lambda functions are triggered by events:
*   **API Gateway**: Expose Lambda as a REST or HTTP API.
*   **S3**: Trigger validation/processing when a file is uploaded (`ObjectCreated`).
*   **DynamoDB Streams**: React to database changes (CDC - Change Data Capture).
*   **EventBridge (CloudWatch Events)**: Scheduled tasks (Cron jobs) or system events.
*   **SQS**: Process messages from a queue.

## 3. Spring Boot on Lambda
Running Java/Spring on Lambda has specific challenges and patterns.

### The Challenge: Cold Starts
*   **What is it?**: The time takes for AWS to provision a new execution environment, download your code, multiply the JVM, and start your Spring Context.
*   **Impact**: First request can take 5-10+ seconds. Subsequent requests ("Warm Starts") are fast.

### Pattern A: Spring Cloud Function
*   **Concept**: Write business logic as standard Java Functions (`java.util.function`).
*   **Code**:
    ```java
    @Bean
    public Function<String, String> uppercase() {
        return value -> value.toUpperCase();
    }
    ```
*   **Adapter**: `spring-cloud-function-adapter-aws` handles the translation between AWS Lambda events and your Java function.

### Pattern B: Serverless Java Container
*   **Concept**: specific for "Lift and Shift". Run a full Spring MVC/Boot application inside a single Lambda function.
*   **Library**: `aws-serverless-java-container-springboot3`.
*   **How it works**: The library proxies API Gateway requests to the `DispatcherServlet`. Useful for migrating existing APIs without rewriting.

### Optimization: AWS SnapStart
*   **Game Changer for Java**: Significantly reduces cold start latency (often < 500ms).
*   **Mechanism**: AWS starts your function, takes a snapshot of the initialized memory (after Spring Context starts), and caches it.
*   **Requirement**: Enable SnapStart in Lambda configuration. Application must be resilient to creating connections from a restored state (use `CRaC` hooks if needed).

## 4. Interview Tips
*   **Usage**: "Create a thumbnail when an image is uploaded to S3." (Lambda triggered by S3).
*   **Cost**: You pay for **Requests** and **Compute Duration** (GB-seconds). If code doesn't run, you pay zero.
*   **Concurrency**:
    *   **Reserved Concurrency**: Guarantee capacity (and limit max scale).
    *   **Provisioned Concurrency**: Keep instances warm to eliminate cold starts (costs money even when idle).
