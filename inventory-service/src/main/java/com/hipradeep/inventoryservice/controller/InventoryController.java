package com.hipradeep.inventoryservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @GetMapping("/{skuCode}")
    public boolean isInStock(@PathVariable String skuCode) {
        System.out.println("Checking stock for skuCode: " + skuCode);

        // Simulate random failure (approx 50% change of failure)
        if (new Random().nextBoolean()) {
            System.out.println("Simulating failure for skuCode: " + skuCode);
            throw new RuntimeException("Simulated Inventory Service Failure");
        }

        return true;
    }
}
