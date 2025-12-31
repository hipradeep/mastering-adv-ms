package com.hipradeep.inventory.service;

import com.hipradeep.inventory.entity.SeatInventory;
import com.hipradeep.inventory.repository.SeatInventoryRepository;
@Service
@Slf4j
@RequiredArgsConstructor
public class SeatInventoryService {

    private final SeatInventoryRepository seatInventoryRepository;

    @PostConstruct
    public void initSeats() {
        if(seatInventoryRepository.count()==0) {
            List<SeatInventory> seats = Stream.iterate(1L, i -> i + 1)
                    .limit(100)
                    .map(id -> new SeatInventory(null, id, "101", "AVAILABLE", null))
                    .collect(Collectors.toList());
            
            seatInventoryRepository.saveAll(seats);
            log.info("Initialized Seat Inventory with 100 seats for Show 101");
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
