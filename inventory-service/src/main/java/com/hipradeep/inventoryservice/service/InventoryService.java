package com.hipradeep.inventoryservice.service;

import com.hipradeep.inventoryservice.entity.Inventory;
import com.hipradeep.inventoryservice.repository.InventoryRepository;
import com.hipradeep.saga.commons.dto.OrderRequestDto;
import com.hipradeep.saga.commons.event.InventoryEvent;
import com.hipradeep.saga.commons.event.InventoryStatus;
import com.hipradeep.saga.commons.event.PaymentEvent;
import com.hipradeep.saga.commons.event.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.stream.Stream;

@Service
@Slf4j
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @PostConstruct
    public void initInventory() {
        inventoryRepository.saveAll(Stream.of(
                new Inventory(101, 10),
                new Inventory(102, 0),
                new Inventory(103, 100)).toList());
    }

    @Transactional
    public InventoryEvent newPaymentEvent(PaymentEvent paymentEvent) {
        log.info("Inventory Service processing PaymentEvent: {}", paymentEvent);
        OrderRequestDto orderRequestDto = new OrderRequestDto(
                paymentEvent.getPaymentRequestDto().getUserId(),
                paymentEvent.getPaymentRequestDto().getProductId(),
                paymentEvent.getPaymentRequestDto().getAmount(),
                paymentEvent.getPaymentRequestDto().getOrderId());

        return inventoryRepository.findById(orderRequestDto.getProductId())
                .filter(inventory -> inventory.getStock() > 0)
                .map(inventory -> {
                    inventory.setStock(inventory.getStock() - 1);
                    inventoryRepository.save(inventory);
                    return new InventoryEvent(orderRequestDto, InventoryStatus.INVENTORY_UPDATED);
                }).orElse(new InventoryEvent(orderRequestDto, InventoryStatus.INVENTORY_FAILED));
    }
}
