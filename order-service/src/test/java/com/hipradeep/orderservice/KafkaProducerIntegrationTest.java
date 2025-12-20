package com.hipradeep.orderservice;

import com.hipradeep.orderservice.service.KafkaProducerService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@DirtiesContext
public class KafkaProducerIntegrationTest {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @Autowired
    private KafkaProducerService producerService;

    private Consumer<String, String> consumer;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeEach
    public void setup() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
            kafka.getBootstrapServers(), "test-group", "true");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(props);
        consumer = consumerFactory.createConsumer();
        consumer.subscribe(Collections.singleton("order-events-v2"));
    }

    @AfterEach
    public void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    public void testSendMessage() {
        String message = "test-order-created";
        producerService.sendMessage(message);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(consumer, "order-events-v2");
        assertThat(record.value()).isEqualTo(message);
    }
}
