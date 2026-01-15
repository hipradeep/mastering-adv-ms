package com.cdac.hpuat.orchestrator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendMessage(String topic, String key, Object message) {
        log.info("Sending message to topic: {}, key: {}, payload: {}", topic, key, message);
        kafkaTemplate.send(topic, key, message);
    }
}
