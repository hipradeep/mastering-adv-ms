package com.hipradeep.userservice.client;

import com.hipradeep.userservice.dto.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
        name = "order-service",
        //url = "http://localhost:8083",  // Direct URL for testing
        fallback = OrderServiceFallback.class
)  // Eureka service name
public interface OrderServiceFeignClient {

    @GetMapping("/api/orders/customer/{customerId}")
    List<Order> getOrdersByCustomerId(@PathVariable Long customerId);

    @GetMapping("/api/orders/{id}")
    Order getOrderById(@PathVariable Long id);

    @GetMapping("/api/orders")
    List<Order> getAllOrders();

    @PostMapping("/api/orders")
    Order createOrder(@RequestBody Order order);

    @DeleteMapping("/api/orders/{id}")
    void deleteOrder(@PathVariable Long id);
}