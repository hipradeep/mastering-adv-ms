package com.hipradeep.userservice.client;

import com.hipradeep.userservice.dto.OrderDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class OrderClient {

    private final RestTemplate restTemplate;

    public OrderClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackGetRecentOrders")
    @Retry(name = "orderService")
    @RateLimiter(name = "orderService", fallbackMethod = "rateLimitFallback")
    public List<OrderDTO> getRecentOrders(Long userId) {
        System.out.println("Calling Order Service for user: " + userId);
        // Assuming Order Service has an endpoint /api/order/user/{userId}
        // Using List.class here requires some care with type erasure, but for demo it's
        // fine or we use array
        // Better: OrderDTO[] response = ...
        OrderDTO[] orders = restTemplate.getForObject("http://localhost:8083/api/order/user/" + userId,
                OrderDTO[].class);

        if (orders == null) {
            return new ArrayList<>();
        }
        return List.of(orders);
    }

    public String simulateTimeout() {
        System.out.println("Calling slow endpoint in Order Service...");
        return restTemplate.getForObject("http://localhost:8083/api/order/slow", String.class);
    }

    public List<OrderDTO> fallbackGetRecentOrders(Long userId, Throwable t) {
        System.out.println(
                "Circuit Breaker / Retry Fallback: Order service is down or error occurred: " + t.getMessage());
        // Return cached orders or empty list
        return Collections.emptyList();
    }

    // Strictly handle RequestNotPermitted to ensure other errors (like connection
    // refused)
    // propagate to the Retry/CircuitBreaker fallbacks instead of being swallowed
    // here.
    public List<OrderDTO> rateLimitFallback(Long userId, RequestNotPermitted t) {
        System.out.println("Rate Limiter Fallback: Too many requests - " + t.getMessage());
        return Collections.emptyList();
    }

    /*
     * // Using Throwable here makes this a global fallback for all exceptions,
     * masking CircuitBreaker/Retry
     * // if aspects are ordered such that RateLimiter sees the exception.
     * public List<OrderDTO> rateLimitFallback(Long userId, Throwable t) {
     * System.out.println("Rate Limiter Fallback: Too many requests - " +
     * t.getMessage());
     * return Collections.emptyList();
     * }
     */
}
