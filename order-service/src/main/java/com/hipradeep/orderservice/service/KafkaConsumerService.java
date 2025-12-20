package com.hipradeep.orderservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topicPartitions = @TopicPartition(topic = "user-events-v2", partitions = "0"), groupId = "order-group")
    public void listen(String message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        System.out.println("Order Service Received User Event from Partition " + partition + ": " + message);
        if ("fail".equals(message)) {
            throw new RuntimeException("Simulated failure for message: " + message);
        }
    }

    @KafkaListener(topics = "user-events-v2.DLT", groupId = "order-group-dlt")
    public void listenDlt(String message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        System.out.println("Order Service DLT Received from Partition " + partition + ": " + message);
    }
}
