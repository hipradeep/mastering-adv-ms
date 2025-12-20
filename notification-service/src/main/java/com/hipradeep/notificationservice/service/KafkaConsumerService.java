package com.hipradeep.notificationservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void listenUserEvents(String message) {
        System.out.println("Notification Service Received User Event: " + message);
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void listenOrderEvents(String message) {
        System.out.println("Notification Service Received Order Event: " + message);
    }
}
