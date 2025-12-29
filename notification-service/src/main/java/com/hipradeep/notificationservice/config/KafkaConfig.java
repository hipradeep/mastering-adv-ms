package com.hipradeep.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Notification service currently only consumes events.
    // Topics are defined in the producing services (Order, Payment, Inventory).
    // This class is here for consistency and future topic definitions.
}
