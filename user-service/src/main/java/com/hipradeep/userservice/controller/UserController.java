package com.hipradeep.userservice.controller;

import com.hipradeep.userservice.client.OrderClient;
import com.hipradeep.userservice.dto.ApiResponse;
import com.hipradeep.userservice.dto.OrderDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private com.hipradeep.userservice.client.OrderClientJitter orderClientJitter;

    @GetMapping("/{userId}/recent-orders")
    public ApiResponse<List<OrderDTO>> getRecentOrders(@PathVariable Long userId) {
        System.out.println("Processing request to fetch recent orders for user: " + userId);
        List<OrderDTO> orders = orderClient.getRecentOrders(userId);
        return ApiResponse.success(orders);
    }

    @GetMapping("/jitter/{userId}")
    public ApiResponse<List<OrderDTO>> getRecentOrdersJitter(@PathVariable Long userId) {
        System.out.println("Processing Jitter request for user: " + userId);
        List<OrderDTO> orders = orderClientJitter.getRecentOrdersWithJitter(userId);
        return ApiResponse.success(orders);
    }

    @GetMapping("/test-timeout")
    public ApiResponse<String> testTimeout() {
        return ApiResponse.success(orderClient.simulateTimeout());
    }

}