package com.hipradeep.inventoryservice.controller;

import com.hipradeep.inventoryservice.model.Inventory;
import com.hipradeep.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku-code}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@PathVariable("sku-code") String skuCode) {
        log.info("Received request to check stock for SKU: {}", skuCode);
        return inventoryService.isInStock(skuCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addInventory(@RequestBody Inventory inventory) {
        log.info("Received request to add inventory: {}", inventory);
        inventoryService.addInventory(inventory);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Inventory> getAllInventory() {
        log.info("Received request to fetch all inventory");
        return inventoryService.getAllInventory();
    }

    @PutMapping("/decrease")
    @ResponseStatus(HttpStatus.OK)
    public String decreaseStock(@RequestParam String skuCode, @RequestParam Integer quantity) {
        log.info("Received request to decrease stock for SKU: {} by {}", skuCode, quantity);
        boolean success = inventoryService.decreaseStock(skuCode, quantity);
        return success ? "Stock decreased successfully" : "Insufficient stock";
    }
}
