package com.hipradeep.inventory.producer;

import com.hipradeep.saga.commons.KafkaConfigProperties;
import com.hipradeep.saga.commons.event.SeatReservedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatInventoryProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSeatReservedEvent(String bookingId, String userId, String showId, java.util.List<String> seatIds, boolean reserved, long amount) {
        SeatReservedEvent reservedEvent = new SeatReservedEvent(bookingId, userId, showId, seatIds, reserved, amount);
        kafkaTemplate.send(KafkaConfigProperties.SEAT_RESERVED_TOPIC, bookingId, reservedEvent);
        if (reserved) {
            log.info("SeatInventoryProducer: Seats locked and event published for bookingId {}", bookingId);
        } else {
            log.info("SeatInventoryProducer: Seats unavailable for bookingId {}", bookingId);
        }
    }
}
