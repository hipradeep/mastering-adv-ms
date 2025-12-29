package com.hipradeep.orderservice.controller;

import com.hipradeep.orderservice.model.Order;
import com.hipradeep.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String placeOrder(@RequestBody Order orderRequest) {
        log.info("Received request to place order: {}", orderRequest);
        return orderService.placeOrder(orderRequest);
    }
}
