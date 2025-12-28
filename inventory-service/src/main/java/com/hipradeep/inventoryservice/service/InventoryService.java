package com.hipradeep.inventoryservice.service;

import com.hipradeep.inventoryservice.model.Inventory;
import com.hipradeep.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final io.micrometer.tracing.Tracer tracer;

    public boolean isInStock(String skuCode) {
        log.info("Checking stock for skuCode: {}", skuCode);
        return inventoryRepository.findBySkuCode(skuCode).isPresent();
    }

    public void addStock(Inventory inventory) {
        log.info("Adding/Updating stock for skuCode: {}", inventory.getSkuCode());
        Inventory existingInventory = inventoryRepository.findBySkuCode(inventory.getSkuCode())
                .orElse(inventory);

        if (existingInventory.getId() != null) {
            existingInventory.setQuantity(existingInventory.getQuantity() + inventory.getQuantity());
        }

        inventoryRepository.save(existingInventory);
        log.info("Stock updated for skuCode: {}", inventory.getSkuCode());
    }

    public boolean reduceStock(String skuCode, Integer quantity) {
        io.micrometer.tracing.Span newSpan = tracer.nextSpan().name("inventory-reduction");
        try (io.micrometer.tracing.Tracer.SpanInScope ws = tracer.withSpan(newSpan.start())) {
            log.info("Reducing stock for skuCode: {} by {}", skuCode, quantity);
            return inventoryRepository.findBySkuCode(skuCode).map(inventory -> {
                if (inventory.getQuantity() >= quantity) {
                    inventory.setQuantity(inventory.getQuantity() - quantity);
                    inventoryRepository.save(inventory);
                    log.info("Stock reduced successfully");
                    return true;
                } else {
                    log.warn("Insufficient stock for skuCode: {}", skuCode);
                    return false;
                }
            }).orElse(false);
        } finally {
            newSpan.end();
        }
    }
}
