package com.hipradeep.orderservice.controller;

import com.hipradeep.orderservice.service.KafkaProducerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final KafkaProducerService kafkaProducerService;

    public OrderController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @PostMapping("/create")
    public String createOrder(@RequestBody String orderData) {
        String message = "ORDER_CREATED: " + orderData;
        kafkaProducerService.sendMessage(message);
        return "Order created event sent: " + message;
    }

    @PostMapping("/confirm")
    public String confirmOrder(@RequestBody String orderId) {
        String message = "ORDER_CONFIRMED: " + orderId;
        kafkaProducerService.sendMessage(message);
        return "Order confirmed event sent: " + message;
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestBody String orderId) {
        String message = "ORDER_CANCELLED: " + orderId;
        kafkaProducerService.sendMessage(message);
        return "Order cancelled event sent: " + message;
    }
}
