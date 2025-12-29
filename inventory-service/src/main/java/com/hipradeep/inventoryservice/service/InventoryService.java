package com.hipradeep.inventoryservice.service;

import com.hipradeep.inventoryservice.model.Inventory;
import com.hipradeep.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode) {
        log.info("Checking stock for SKU: {}", skuCode);
        return inventoryRepository.findBySkuCode(skuCode).isPresent();
    }

    @Transactional
    public void addInventory(Inventory inventory) {
        log.info("Adding inventory: {}", inventory);
        inventoryRepository.save(inventory);
        log.info("Inventory added successfully");
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAllInventory() {
        log.info("Fetching all inventory items");
        return inventoryRepository.findAll();
    }

    @Transactional
    public boolean decreaseStock(String skuCode, Integer quantity) {
        log.info("Decreasing stock for SKU: {} by {}", skuCode, quantity);
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (inventory.getQuantity() >= quantity) {
            inventory.setQuantity(inventory.getQuantity() - quantity);
            inventoryRepository.save(inventory);
            log.info("Stock decreased successfully for SKU: {}", skuCode);
            return true;
        } else {
            log.warn("Insufficient stock for SKU: {}. Requested: {}, Available: {}", skuCode, quantity,
                    inventory.getQuantity());
            return false;
        }
    }
}
