package com.hipradeep.inventoryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "INVENTORY_TBL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    @Id
    private Integer productId;
    private Integer stock;
}
