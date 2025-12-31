package com.hipradeep.inventory.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seat_inventory")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long seatId;
    private String showId;
    private String status; // AVAILABLE, LOCKED, BOOKED
    private String bookingId;
}
