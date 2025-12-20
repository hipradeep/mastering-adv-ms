package com.hipradeep.orderservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "user-events", groupId = "order-group")
    public void listen(String message) {
        System.out.println("Order Service Received User Event: " + message);
    }
}
