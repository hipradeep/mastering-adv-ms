package com.hipradeep.userservice.client;

import com.hipradeep.userservice.dto.OrderDTO;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class OrderClientJitter {

    private final RestTemplate restTemplate;

    public OrderClientJitter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Retry(name = "orderServiceJitter", fallbackMethod = "fallbackGetRecentOrders")
    public List<OrderDTO> getRecentOrdersWithJitter(Long userId) {
        System.out.println("Calling Order Service (Jitter Config) for user: " + userId);
        // Using the same endpoint, but the retry logic wrapping this call will be
        // different
        OrderDTO[] orders = restTemplate.getForObject("http://localhost:8083/api/order/user/" + userId,
                OrderDTO[].class);

        if (orders == null) {
            return new ArrayList<>();
        }
        return List.of(orders);
    }

    public List<OrderDTO> fallbackGetRecentOrders(Long userId, Throwable t) {
        System.out.println("Jitter Client Fallback: Order service is down or error occurred: " + t.getMessage());
        return Collections.emptyList();
    }
}
