package com.hipradeep.inventory.repository;

import com.hipradeep.inventory.entity.SeatInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {
    List<SeatInventory> findByShowId(String showId);
    List<SeatInventory> findByBookingId(String bookingId);
    long countByShowId(String showId);
}
