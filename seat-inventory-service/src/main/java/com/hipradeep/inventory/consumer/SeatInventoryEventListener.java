package com.hipradeep.inventory.consumer;

import com.hipradeep.inventory.entity.SeatInventory;
import com.hipradeep.inventory.producer.SeatInventoryProducer;
import com.hipradeep.inventory.repository.SeatInventoryRepository;
import com.hipradeep.saga.commons.KafkaConfigProperties;
import com.hipradeep.saga.commons.event.BookingCreatedEvent;
import com.hipradeep.saga.commons.event.BookingPaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatInventoryEventListener {

    private final SeatInventoryRepository seatInventoryRepository;
    private final SeatInventoryProducer seatInventoryProducer;

    /**
     * Listen for Booking Created Events to reserve seats.
     * Topic: booking-created-events (KafkaConfigProperties.BOOKING_CREATED_TOPIC)
     * Action:
     *  - Verify seat availability.
     *  - If available -> Lock seats & publish SeatReservedEvent(reserved=true).
     *  - If unavailable -> publish SeatReservedEvent(reserved=false).
     */
    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.BOOKING_CREATED_TOPIC, groupId = KafkaConfigProperties.INVENTORY_GROUP_ID)
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("SeatInventoryEventListener: Received BookingCreatedEvent: {}", event);

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
            
            seatInventoryProducer.publishSeatReservedEvent(event.bookingId(), event.userId(), true, event.amount());
        } else {
            seatInventoryProducer.publishSeatReservedEvent(event.bookingId(), event.userId(), false, event.amount());
        }
    }

    /**
     * Listen for Payment Events to finalize seat status.
     * Topic: booking-payment-events (KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC)
     * Action:
     *  - If payment success -> Mark seats as BOOKED.
     *  - If payment failed -> Release seats (set to AVAILABLE).
     */
    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.PAYMENT_PROCESSED_TOPIC, groupId = KafkaConfigProperties.INVENTORY_GROUP_ID)
    public void handlePaymentEvent(BookingPaymentEvent event) {
        log.info("SeatInventoryEventListener: Received Payment Event: {}", event);
        
        List<SeatInventory> lockedSeats = seatInventoryRepository.findByBookingId(event.bookingId());
        
        if (event.paymentCompleted()) {
            lockedSeats.forEach(s -> s.setStatus("BOOKED"));
            log.info("SeatInventoryEventListener: Seats BOOKED for bookingId {}", event.bookingId());
        } else {
            lockedSeats.forEach(s -> {
                s.setStatus("AVAILABLE");
                s.setBookingId(null);
            });
            log.info("SeatInventoryEventListener: Seats RELEASED for failed bookingId {}", event.bookingId());
        }
        seatInventoryRepository.saveAll(lockedSeats);
    }
}
