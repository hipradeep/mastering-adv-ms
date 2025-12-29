package com.hipradeep.orderservice.service;

import com.hipradeep.orderservice.client.InventoryClient;
import com.hipradeep.orderservice.model.Order;
import com.hipradeep.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    public String placeOrder(Order orderRequest) {
        log.info("Attempting to place order for SKU: {}", orderRequest.getSkuCode());
        orderRequest.setOrderNumber(UUID.randomUUID().toString());

        // Check Inventory
        boolean inStock = inventoryClient.isInStock(orderRequest.getSkuCode());

        if (inStock) {
            // Decrease Stock
            String result = inventoryClient.decreaseStock(orderRequest.getSkuCode(), orderRequest.getQuantity());
            if ("Insufficient stock".equals(result)) {
                log.warn("Insufficient stock for SKU: {}", orderRequest.getSkuCode());
                throw new IllegalArgumentException("Insufficient stock for product: " + orderRequest.getSkuCode());
            }

            // Save Order
            orderRepository.save(orderRequest);

            // Logging
            log.info("Order placed successfully with Order Number: {}", orderRequest.getOrderNumber());

            return "Order Placed Successfully";
        } else {
            log.warn("Product not in stock: {}", orderRequest.getSkuCode());
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }
    }
}
