package com.hipradeep.booking.producer;

import com.hipradeep.saga.commons.KafkaConfigProperties;
import com.hipradeep.saga.commons.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBookingCreatedEvent(BookingCreatedEvent event) {
        kafkaTemplate.send(KafkaConfigProperties.BOOKING_CREATED_TOPIC, event);
        log.info("BookingProducer: Published BookingCreatedEvent: {}", event);
    }
}
