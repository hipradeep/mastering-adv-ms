package com.hipradeep.orderservice.controller;

import com.hipradeep.orderservice.client.InventoryClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private com.hipradeep.orderservice.client.InventoryClientJitter inventoryClientJitter;

    @PostMapping
    public String placeOrder(@RequestParam String skuCode) {
        boolean inStock = inventoryClient.checkStock(skuCode);

        if (inStock) {
            return "Order placed successfully for " + skuCode;
        } else {
            return "Order failed. Item " + skuCode + " is out of stock or service is down (Fallback executed).";
        }
    }

    @PostMapping("/jitter")
    public String placeOrderJitter(@RequestParam String skuCode) {
        boolean inStock = inventoryClientJitter.checkStockWithJitter(skuCode);

        if (inStock) {
            return "Order (Jitter) placed successfully for " + skuCode;
        } else {
            return "Order (Jitter) failed. Item " + skuCode
                    + " is out of stock or service is down (Fallback executed).";
        }
    }

    // Dummy DTO for internal usage
    @Data
    @AllArgsConstructor
    static class OrderResponse {
        private String orderId;
        private String item;
        private Double amount;
    }

    @GetMapping("/user/{userId}")
    public List<OrderResponse> getOrdersByUser(
            @PathVariable Long userId) {
        // Simulate fetching orders for user
        return List.of(
                new OrderResponse("ORD-101", "MacBook Pro", 2500.0),
                new OrderResponse("ORD-102", "iPhone 15", 1000.0));
    }

    @GetMapping("/slow")
    public String getSlowResponse() throws InterruptedException {
        // Simulate a 5-second delay
        Thread.sleep(5000);
        return "Sorry for being late!";
    }
}
