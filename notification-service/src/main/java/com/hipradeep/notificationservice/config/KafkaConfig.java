package com.hipradeep.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name("notification-events-v2")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userEventsDltTopic() {
        return TopicBuilder.name("user-events-v2.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderEventsDltTopic() {
        return TopicBuilder.name("order-events-v2.DLT")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        // FixedBackOff: 1 second interval, 3 attempts
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 3);
        
        // DeadLetterPublishingRecoverer sends messages to DLT after retries are exhausted
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        
        return new DefaultErrorHandler(recoverer, fixedBackOff);
    }
}
