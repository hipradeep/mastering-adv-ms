package com.hipradeep.userservice.client;

import com.hipradeep.userservice.dto.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class OrderServiceFallbackFactory implements FallbackFactory<OrderServiceFeignClient> {

    @Override
    public OrderServiceFeignClient create(Throwable cause) {
        return new OrderServiceFeignClient() {
            @Override
            public List<Order> getOrdersByCustomerId(Long customerId) {
                log.error("🎯 FALLBACK TRIGGERED for getOrdersByCustomerId({}). Cause: {}", customerId, cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public Order getOrderById(Long id) {
                log.error("🎯 FALLBACK TRIGGERED for getOrderById({}). Cause: {}", id, cause.getMessage());
                // Return a default order or null
                return null; // or new Order() with default values
            }

            @Override
            public List<Order> getAllOrders() {
                log.error("🎯 FALLBACK TRIGGERED for getAllOrders(). Cause: {}", cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public Order createOrder(Order order) {
                log.error("🎯 FALLBACK TRIGGERED for createOrder. Cause: {}", cause.getMessage());
                throw new RuntimeException("Order Service unavailable: " + cause.getMessage());
            }

            @Override
            public void deleteOrder(Long id) {
                log.error("🎯 FALLBACK TRIGGERED for deleteOrder({}). Cause: {}", id, cause.getMessage());
                throw new RuntimeException("Order Service unavailable: " + cause.getMessage());
            }
        };
    }
}