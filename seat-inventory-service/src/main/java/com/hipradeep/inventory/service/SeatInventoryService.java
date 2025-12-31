package com.hipradeep.inventory.service;

import com.hipradeep.inventory.entity.SeatInventory;
import com.hipradeep.inventory.repository.SeatInventoryRepository;
import com.hipradeep.saga.commons.event.BookingCreatedEvent;
import com.hipradeep.saga.commons.event.BookingPaymentEvent;
import com.hipradeep.saga.commons.event.SeatReservedEvent;
import com.hipradeep.saga.commons.KafkaConfigProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatInventoryService {

    private final SeatInventoryRepository seatInventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostConstruct
    public void initSeats() {
        if(seatInventoryRepository.count()==0) {
            seatInventoryRepository.saveAll(Stream.of(
                    new SeatInventory(null, 1L, "101", "AVAILABLE", null),
                    new SeatInventory(null, 2L, "101", "AVAILABLE", null),
                    new SeatInventory(null, 3L, "101", "AVAILABLE", null),
                    new SeatInventory(null, 4L, "101", "AVAILABLE", null),
                    new SeatInventory(null, 5L, "101", "AVAILABLE", null)
            ).collect(Collectors.toList()));
        }
    }

    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.BOOKING_CREATED_TOPIC, groupId = KafkaConfigProperties.INVENTORY_GROUP_ID)
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("SeatInventory: Received BookingCreatedEvent: {}", event);

        List<Long> requestedSeats = event.seatIds().stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        List<SeatInventory> showSeats = seatInventoryRepository.findByShowId(event.showId());
        
        List<SeatInventory> targetSeats = showSeats.stream()
                .filter(s -> requestedSeats.contains(s.getSeatId()))
                .collect(Collectors.toList());

        boolean allAvailable = targetSeats.size() == requestedSeats.size() &&
                targetSeats.stream().allMatch(s -> "AVAILABLE".equals(s.getStatus()));

        if (allAvailable) {
            targetSeats.forEach(s -> {
                s.setStatus("LOCKED");
                s.setBookingId(event.bookingId());
            });
            seatInventoryRepository.saveAll(targetSeats);
            
            SeatReservedEvent reservedEvent = new SeatReservedEvent(event.bookingId(), event.userId(), true, event.amount());
            kafkaTemplate.send(KafkaConfigProperties.SEAT_RESERVED_TOPIC, reservedEvent);
            log.info("SeatInventory: Seats locked and event published for bookingId {}", event.bookingId());
        } else {
            SeatReservedEvent reservedEvent = new SeatReservedEvent(event.bookingId(), event.userId(), false, event.amount());
            kafkaTemplate.send(KafkaConfigProperties.SEAT_RESERVED_TOPIC, reservedEvent);
            log.info("SeatInventory: Seats unavailable for bookingId {}", event.bookingId());
        }
    }

    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC, groupId = KafkaConfigProperties.INVENTORY_GROUP_ID)
    public void handlePaymentEvent(BookingPaymentEvent event) {
        log.info("SeatInventory: Received Payment Event: {}", event);
        
        List<SeatInventory> lockedSeats = seatInventoryRepository.findByBookingId(event.bookingId());
        
        if (event.paymentCompleted()) {
            lockedSeats.forEach(s -> s.setStatus("BOOKED"));
            log.info("SeatInventory: Seats BOOKED for bookingId {}", event.bookingId());
        } else {
            lockedSeats.forEach(s -> {
                s.setStatus("AVAILABLE");
                s.setBookingId(null);
            });
            log.info("SeatInventory: Seats RELEASED for failed bookingId {}", event.bookingId());
        }
        seatInventoryRepository.saveAll(lockedSeats);
    }
}
