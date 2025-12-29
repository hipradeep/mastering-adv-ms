package com.hipradeep.orderservice.service;

import com.hipradeep.orderservice.entity.Order;
import com.hipradeep.orderservice.repository.OrderRepository;
import com.hipradeep.saga.commons.dto.OrderRequestDto;
import com.hipradeep.saga.commons.event.OrderStatus;
import com.hipradeep.saga.commons.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StreamBridge streamBridge;

    @Transactional
    public Order createOrder(OrderRequestDto orderRequestDto) {
        log.info("Creating new Order: {}", orderRequestDto);
        Order order = orderRepository.save(convertDtoToEntity(orderRequestDto));
        orderRequestDto.setOrderId(order.getId());
        // produce kafka event with status ORDER_CREATED
        log.info("Publishing OrderEvent for OrderId: {} via StreamBridge", order.getId());
        streamBridge.send("orderSupplier-out-0", new OrderEvent(orderRequestDto, OrderStatus.ORDER_CREATED));
        return order;
    }

    private Order convertDtoToEntity(OrderRequestDto dto) {
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setProductId(dto.getProductId());
        order.setPrice(dto.getAmount());
        order.setOrderStatus(OrderStatus.ORDER_CREATED);
        // order.setPaymentStatus(PaymentStatus.PAYMENT_FAILED); // Initially failed
        // until paid? Or default?
        // Let's assume initialized null or some default.
        return order;
    }
}
