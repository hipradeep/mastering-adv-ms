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

    @GetMapping("/{userId}/recent-orders")
    public ApiResponse<List<OrderDTO>> getRecentOrders(@PathVariable Long userId) {
        System.out.println("Processing request to fetch recent orders for user: " + userId);
        List<OrderDTO> orders = orderClient.getRecentOrders(userId);
        return ApiResponse.success(orders);
    }

    @GetMapping("/test-timeout")
    public ApiResponse<String> testTimeout() {
        return ApiResponse.success(orderClient.simulateTimeout());
    }

}