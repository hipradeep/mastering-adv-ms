package com.hipradeep.orderservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send("order-events", message);
    }

    public void sendMessage(String message, Integer partition) {
        if (partition != null) {
            kafkaTemplate.send("order-events", partition, null, message);
        } else {
            kafkaTemplate.send("order-events", message);
        }
    }
}
