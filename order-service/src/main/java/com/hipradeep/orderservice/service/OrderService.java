package com.hipradeep.orderservice.service;

import com.hipradeep.orderservice.dto.Order;
import com.hipradeep.orderservice.dto.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final Map<Long, Order> orders = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public OrderService() {
        initializeDummyData();
    }

    private void initializeDummyData() {
        // Create some dummy orders
        Order order1 = Order.builder()
                .id(idCounter.getAndIncrement())
                .orderNumber("ORD-001")
                .customerId(1L)
                .customerName("John Doe")
                .status("PENDING")
                .totalAmount(new BigDecimal("150.50"))
                .orderDate(LocalDateTime.now().minusDays(2))
                .shippingAddress("123 Main St, New York, NY")
                .items(Arrays.asList(
                        OrderItem.builder()
                                .productId(101L)
                                .productName("Laptop")
                                .quantity(1)
                                .price(new BigDecimal("1200.00"))
                                .subtotal(new BigDecimal("1200.00"))
                                .build(),
                        OrderItem.builder()
                                .productId(102L)
                                .productName("Mouse")
                                .quantity(2)
                                .price(new BigDecimal("25.25"))
                                .subtotal(new BigDecimal("50.50"))
                                .build()
                ))
                .build();

        Order order2 = Order.builder()
                .id(idCounter.getAndIncrement())
                .orderNumber("ORD-002")
                .customerId(2L)
                .customerName("Jane Smith")
                .status("COMPLETED")
                .totalAmount(new BigDecimal("75.99"))
                .orderDate(LocalDateTime.now().minusDays(1))
                .shippingAddress("456 Oak St, Los Angeles, CA")
                .items(Arrays.asList(
                        OrderItem.builder()
                                .productId(103L)
                                .productName("Keyboard")
                                .quantity(1)
                                .price(new BigDecimal("75.99"))
                                .subtotal(new BigDecimal("75.99"))
                                .build()
                ))
                .build();

        orders.put(order1.getId(), order1);
        orders.put(order2.getId(), order2);
    }

    // Create
    public Order createOrder(Order order) {
        Long newId = idCounter.getAndIncrement();
        order.setId(newId);
        order.setOrderNumber("ORD-" + String.format("%03d", newId));
        order.setOrderDate(LocalDateTime.now());
        orders.put(newId, order);
        return order;
    }

    // Read - Get all
    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    // Read - Get by ID
    public Optional<Order> getOrderById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    // Read - Get by customer ID
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orders.values().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .toList();
    }

    // Update
    public Optional<Order> updateOrder(Long id, Order orderDetails) {
        return Optional.ofNullable(orders.computeIfPresent(id, (key, existingOrder) -> {
            existingOrder.setCustomerId(orderDetails.getCustomerId());
            existingOrder.setCustomerName(orderDetails.getCustomerName());
            existingOrder.setStatus(orderDetails.getStatus());
            existingOrder.setTotalAmount(orderDetails.getTotalAmount());
            existingOrder.setItems(orderDetails.getItems());
            existingOrder.setShippingAddress(orderDetails.getShippingAddress());
            return existingOrder;
        }));
    }

    // Update status only
    public Optional<Order> updateOrderStatus(Long id, String status) {
        return Optional.ofNullable(orders.computeIfPresent(id, (key, order) -> {
            order.setStatus(status);
            return order;
        }));
    }

    // Delete
    public boolean deleteOrder(Long id) {
        return orders.remove(id) != null;
    }
}