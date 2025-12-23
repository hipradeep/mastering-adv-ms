# Mastering Advanced Microservices

A multi-module Maven project demonstrating microservices architecture.

# Spring Retry Simulation

This project demonstrates a Spring Retry implementation where `order-service` calls `inventory-service` with automatic retries upon failure.

## Overview
- **Order Service**: Acts as the consumer. It uses `RestTemplate` to call the Inventory Service. It is configured with `@Retryable` to retry failed requests up to 3 times before triggering a `@Recover` fallback method.
- **Inventory Service**: Acts as the provider. It has a simulation endpoint that randomly throws a runtime exception (approx. 50% of the time) to mimic service instability.

## API Flow
1. **Request**: Client sends a POST request to `order-service`: `http://localhost:8083/api/order?skuCode=iphone-13`
2. **Internal Call**: `order-service` calls `inventory-service`: `http://localhost:8084/api/inventory/iphone-13`
3. **Failure Simulation**: `inventory-service` randomizes logic. If it fails, it throws a `RuntimeException`.
4. **Retry Mechanism**:
    - `order-service` detects the exception.
    - It waits for a configured backoff period (1 second).
    - It retries the call up to 3 times.
5. **Fallback**: If all 3 attempts fail, the `recover` method is executed, returning a default "fallback" response to the client.

## Testing the Simulation
1. Start **Inventory Service** (Port 8084).
2. Start **Order Service** (Port 8083).
3. Send a request to Order Service:
    ```bash
    curl -X POST "http://localhost:8083/api/order?skuCode=test-product"
    ```
4. **Observe Logs** in `order-service`:
    - **Success Case**: You see a single "Calling Inventory Service..." log.
    - **Retry Case**: You see "Calling Inventory Service..." printed multiple times (e.g., 2 or 3 times).
    - **Failure Case**: You see "Calling Inventory Service..." 3 times, followed by "Max retries reached...".

# Resilience4j Implementation

This project now includes Resilience4j patterns (Circuit Breaker, Retry, Rate Limiter) in `user-service`.

## Overview
- **User Service**: Calls `order-service` to fetch recent orders.
    - **Circuit Breaker**: Trips open if failure rate exceeds 50% (min 5 calls). Configured with 5s wait time.
    - **Retry**: Retries up to 3 times with 1s wait duration.
    - **Rate Limiter**: Limits requests to 5 per 10 seconds.
- **Order Service**: Provides a simulation endpoint to return recent orders.

## API Flow
1. **Request**: `GET http://localhost:8082/api/users/{userId}/recent-orders`
2. **Internal Call**: `user-service` calls `order-service` at `/api/order/user/{userId}`
3. **Resilience**:
    - If `order-service` is down or fails, Retry attempts 3 times.
    - If failures persist, Circuit Breaker opens and subsequent calls fail fast (until timeout).
    - If traffic is too high (burst), Rate Limiter rejects calls immediately.
4. **Fallback**: All patterns have fallback methods returning an empty list.

## Testing
1. Start `order-service` (8083) and `user-service` (8082).
2. **Normal Flow**:
   ```bash
   curl http://localhost:8082/api/users/1/recent-orders
   ```
3. **Test Circuit Breaker/Retry**:
   - Stop `order-service`.
   - Call `user-service`. Check logs for retry attempts.
   - Make >5 calls. Observe "Circuit Breaker Open" logs and immediate fallbacks.
6. **Test Rate Limiter**:
   - Use a tool (like Apache Bench or rapid curl) to send >5 requests in <10s.
   - Observe "Rate Limiter" fallback messages.

## Detailed Fallback Testing
To Verify that **Connection Refused** errors are NOT swallowed by the Rate Limiter:
1. **Bad URL Method**: Change the URL in `OrderClient.java` to an invalid one (e.g., `.../api/order/use1r/...`).
2. **Stop Service Method**: Stop the `order-service` entirely.


## Technical Note: Fallback Handling

When implementing fallbacks for Rate Limiters, it is crucial to catch the specific exception `RequestNotPermitted`.

- **Incorrect Approach**: Using `Throwable t` as the fallback argument catch-all errors. This causes the Rate Limiter fallback to swallow unrelated exceptions (like connection refused or timeouts) that should ideally trigger the Circuit Breaker or Retry logic.
- **Correct Approach**: Using `RequestNotPermitted t` as the fallback argument ensures that the method is *only* invoked for rate limiting violations. Other exceptions propagate normally, allowing other resilience patterns (Circuit Breaker/Retry) to handle them appropriately.

## Client-Side Timeout Demonstration

To demonstrate configuring timeouts on the `RestTemplate` (Client):

1. **Configuration**: The `user-service` `RestTemplate` is configured with a **2-second timeout** (connect & read).
2. **Simulation**: The `order-service` has a `/api/order/slow` endpoint that sleeps for **5 seconds**.
3. **Testing**:
    - Ensure both services are running.
    - Call the test endpoint: `curl http://localhost:8082/api/users/test-timeout`
    - **Result**: The request will fail after ~2 seconds with a `ResourceAccessException` (Read timed out), proving that the client correctly gave up waiting for the slow server.

## Jitter (Randomized Backoff) Demonstration

To demonstrate preventing thundering herd problems using Jitter:

1.  **User Service (Resilience4j)**:
    - New endpoint: `/api/users/jitter/{userId}`
    - Logic: Uses `OrderClientJitter`.
    - **Note**: Config is **externalized** in `application.yml` (under `orderServiceJitter`), NOT in the Java annotation.
    - Config: `waitDuration: 1s`, `randomizedWaitFactor: 0.5` -> Wait time is random between 0.5s and 1.5s.
    - Test: `curl http://localhost:8082/api/users/jitter/1`

2.  **Order Service (Spring Retry)**:
    - New endpoint: `/api/order/jitter?skuCode=...`
    - Logic: Uses `InventoryClientJitter` with `@Backoff(random = true)`.
    - Config: Random delay between 1000ms and 3000ms.
    - Test: `curl -X POST "http://localhost:8083/api/order/jitter?skuCode=iphone-13"`
