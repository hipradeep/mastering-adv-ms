package com.hipradeep.inventoryservice.controller;

import com.hipradeep.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku-code}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@PathVariable("sku-code") String skuCode) {
        log.info("Received inventory check request for skuCode: {}", skuCode);
        return inventoryService.isInStock(skuCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String addStock(@RequestBody com.hipradeep.inventoryservice.model.Inventory inventory) {
        inventoryService.addStock(inventory);
        return "Stock Added/Updated Successfully";
    }

    @PutMapping("/reduce/{sku-code}")
    @ResponseStatus(HttpStatus.OK)
    public boolean reduceStock(@PathVariable("sku-code") String skuCode, @RequestParam Integer quantity) {
        return inventoryService.reduceStock(skuCode, quantity);
    }
}
