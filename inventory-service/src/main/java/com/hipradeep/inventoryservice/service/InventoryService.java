package com.hipradeep.inventoryservice.service;

import com.hipradeep.inventoryservice.model.Inventory;
import com.hipradeep.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public Inventory createInventory(Inventory inventory) {
        log.info("Creating inventory for sku: {}", inventory.getSkuCode());
        return inventoryRepository.save(inventory);
    }

    public List<Inventory> getAllInventory() {
        log.info("Fetching all inventory");
        return inventoryRepository.findAll();
    }

    public Inventory getInventoryById(Long id) {
        log.info("Fetching inventory by id: {}", id);
        return inventoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Inventory not found"));
    }

    public void deleteInventory(Long id) {
        log.info("Deleting inventory with id: {}", id);
        inventoryRepository.deleteById(id);
    }
}
