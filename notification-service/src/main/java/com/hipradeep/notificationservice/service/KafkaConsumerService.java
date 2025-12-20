package com.hipradeep.notificationservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    @KafkaListener(topicPartitions = @TopicPartition(topic = "user-events", partitions = "0"), groupId = "notification-group")
    public void listenUserEvents(String message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        System.out.println("Notification Service Received User Event from Partition " + partition + ": " + message);
    }

    @KafkaListener(topicPartitions = @TopicPartition(topic = "order-events", partitions = "0"), groupId = "notification-group")
    public void listenOrderEvents(String message, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {
        System.out.println("Notification Service Received Order Event from Partition " + partition + ": " + message);
    }
}
