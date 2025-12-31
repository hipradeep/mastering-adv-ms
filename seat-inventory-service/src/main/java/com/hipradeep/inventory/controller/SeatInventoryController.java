package com.hipradeep.inventory.controller;

import com.hipradeep.inventory.entity.SeatInventory;
import com.hipradeep.inventory.service.SeatInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class SeatInventoryController {

    private final SeatInventoryService seatInventoryService;

    @PostMapping
    public SeatInventory addSeat(@RequestBody SeatInventory seatInventory) {
        return seatInventoryService.addSeat(seatInventory);
    }

    @GetMapping
    public List<SeatInventory> getAllSeats() {
        return seatInventoryService.getAllSeats();
    }

    @GetMapping("/show/{showId}")
    public List<SeatInventory> getSeatsByShowId(@PathVariable String showId) {
        return seatInventoryService.getSeatsByShowId(showId);
    }

    @PutMapping("/{id}/status")
    public SeatInventory updateSeatStatus(@PathVariable Long id, @RequestParam String status) {
        return seatInventoryService.updateSeatStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void deleteSeat(@PathVariable Long id) {
        seatInventoryService.deleteSeat(id);
    }
}
