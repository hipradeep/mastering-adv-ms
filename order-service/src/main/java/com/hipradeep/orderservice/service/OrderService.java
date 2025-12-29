package com.hipradeep.orderservice.service;

import com.hipradeep.orderservice.model.Order;
import com.hipradeep.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(Order order) {
        log.info("Creating order: {}", order.getOrderNumber());
        return orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        log.info("Fetching all orders");
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        log.info("Fetching order by id: {}", id);
        return orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);
        orderRepository.deleteById(id);
    }
}
