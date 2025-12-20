package com.hipradeep.userservice.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send("user-events", message);
    }

    public void sendMessage(String message, Integer partition) {
        if (partition != null) {
            kafkaTemplate.send("user-events", partition, null, message);
        } else {
            kafkaTemplate.send("user-events", message);
        }
    }
}
