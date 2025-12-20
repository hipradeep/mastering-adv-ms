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
    public String createOrder(@RequestBody String orderData, @RequestParam(required = false) Integer partition) {
        String message = "ORDER_CREATED: " + orderData;
        kafkaProducerService.sendMessage(message, partition);
        return "Order created event sent to partition " + (partition != null ? partition : "default") + ": " + message;
    }

    @PostMapping("/confirm")
    public String confirmOrder(@RequestBody String orderId, @RequestParam(required = false) Integer partition) {
        String message = "ORDER_CONFIRMED: " + orderId;
        kafkaProducerService.sendMessage(message, partition);
        return "Order confirmed event sent to partition " + (partition != null ? partition : "default") + ": " + message;
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestBody String orderId, @RequestParam(required = false) Integer partition) {
        String message = "ORDER_CANCELLED: " + orderId;
        kafkaProducerService.sendMessage(message, partition);
        return "Order cancelled event sent to partition " + (partition != null ? partition : "default") + ": " + message;
    }
}
