package com.hipradeep.inventory.service;

import com.hipradeep.inventory.entity.SeatInventory;
import com.hipradeep.inventory.repository.SeatInventoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeatInventoryService {

    private final SeatInventoryRepository seatInventoryRepository;

    @PostConstruct
    public void initSeats() {
        // Init Show 101
        if(seatInventoryRepository.countByShowId("101") == 0) {
            List<SeatInventory> seats101 = Stream.iterate(1L, i -> i + 1)
                    .limit(100)
                    .map(id -> new SeatInventory(null, id, "101", "AVAILABLE", null))
                    .collect(Collectors.toList());
            seatInventoryRepository.saveAll(seats101);
            log.info("Initialized Seat Inventory with 100 seats for Show 101");
        }

        // Init Show 102
        if(seatInventoryRepository.countByShowId("102") == 0) {
            List<SeatInventory> seats102 = Stream.iterate(101L, i -> i + 1)
                    .limit(100)
                    .map(id -> new SeatInventory(null, id, "102", "AVAILABLE", null))
                    .collect(Collectors.toList());
            seatInventoryRepository.saveAll(seats102);
            log.info("Initialized Seat Inventory with 100 seats for Show 102");
        }
    }

    // CRUD Methods

    public SeatInventory addSeat(SeatInventory seatInventory) {
        return seatInventoryRepository.save(seatInventory);
    }

    public List<SeatInventory> getAllSeats() {
        return seatInventoryRepository.findAll();
    }

    public List<SeatInventory> getSeatsByShowId(String showId) {
        return seatInventoryRepository.findByShowId(showId);
    }
    
    public SeatInventory updateSeatStatus(Long id, String status) {
        return seatInventoryRepository.findById(id).map(seat -> {
            seat.setStatus(status);
            return seatInventoryRepository.save(seat);
        }).orElseThrow(() -> new RuntimeException("Seat not found with id: " + id));
    }

    public void deleteSeat(Long id) {
        seatInventoryRepository.deleteById(id);
    }
}
