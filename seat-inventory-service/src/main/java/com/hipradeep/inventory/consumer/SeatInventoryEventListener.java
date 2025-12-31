package com.hipradeep.inventory.consumer;

import com.hipradeep.inventory.entity.SeatInventory;
import com.hipradeep.inventory.producer.SeatInventoryProducer;
import com.hipradeep.inventory.repository.SeatInventoryRepository;
import com.hipradeep.saga.commons.KafkaConfigProperties;

import com.hipradeep.saga.commons.event.SeatReservedEvent;
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

    @Transactional
    @KafkaListener(topics = KafkaConfigProperties.SEAT_RESERVED_CMD_TOPIC, groupId = KafkaConfigProperties.INVENTORY_GROUP_ID)
    public void handleSeatReservedCommand(SeatReservedEvent event) {
        log.info("SeatInventoryEventListener: Received Command: {}", event);

        if (event.reserved()) {
            handleReservation(event);
        } else {
            handleRelease(event);
        }
    }

    private void handleReservation(SeatReservedEvent event) {
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
            
            // Reply with Success
            seatInventoryProducer.publishSeatReservedEvent(event.bookingId(), event.userId(), event.showId(), event.seatIds(), true, event.amount());
        } else {
            // Reply with Failure
            seatInventoryProducer.publishSeatReservedEvent(event.bookingId(), event.userId(), event.showId(), event.seatIds(), false, event.amount());
        }
    }

    private void handleRelease(SeatReservedEvent event) {
        log.info("SeatInventoryEventListener: Rolling back (releasing) seats for bookingId: {}", event.bookingId());
        List<SeatInventory> lockedSeats = seatInventoryRepository.findByBookingId(event.bookingId());
        
        lockedSeats.forEach(s -> {
            s.setStatus("AVAILABLE");
            s.setBookingId(null);
        });
        seatInventoryRepository.saveAll(lockedSeats);
    }
}
