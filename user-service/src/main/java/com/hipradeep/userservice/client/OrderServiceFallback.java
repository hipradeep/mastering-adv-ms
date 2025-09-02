package com.hipradeep.userservice.client;

import com.hipradeep.userservice.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class OrderServiceFallback implements OrderServiceFeignClient {

    @Override
    public List<Order> getOrdersByCustomerId(Long customerId) {
        log.warn("Fallback: Unable to fetch orders for customer {}", customerId);
        return Collections.emptyList();
    }

    @Override
    public Order getOrderById(Long id) {
        log.warn("Fallback: Unable to fetch order with ID: {}", id);
        return createDummyOrder(id);
    }

    @Override
    public List<Order> getAllOrders() {
        log.warn("Fallback: Unable to fetch all orders");
        return Collections.emptyList();
    }

    @Override
    public Order createOrder(Order order) {
        log.warn("Fallback: Unable to create order. Service unavailable");
        // Return the input order with a fallback message or null
        if (order != null) {
            order.setStatus("CREATION_FAILED - Service unavailable");
        }
        return order;
    }

    @Override
    public void deleteOrder(Long id) {
        log.warn("Fallback: Unable to delete order with ID: {}. Service unavailable", id);
        // In a fallback, we can't actually delete, so we just log and do nothing
    }

    private Order createDummyOrder(Long orderId) {
        // Create a dummy order for fallback purposes
        return Order.builder()
                .id(orderId != null ? orderId : -1L)
                .customerId(-1L)
                .status("FALLBACK_ORDER - Service unavailable")
                .totalAmount(BigDecimal.valueOf(0.0))
                .items(Collections.emptyList())
                .build();
    }

    // Optional: Add a method to check if we're in fallback mode
    public boolean isFallbackActive() {
        log.info("Fallback mode is active for OrderService");
        return true;
    }

    // Optional: Add method to get fallback statistics
    public String getFallbackStatus() {
        return "OrderServiceFallback is active due to service unavailability";
    }
}