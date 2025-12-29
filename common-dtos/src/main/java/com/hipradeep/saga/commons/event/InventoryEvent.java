package com.hipradeep.saga.commons.event;

import com.hipradeep.saga.commons.dto.OrderRequestDto;
import com.hipradeep.saga.commons.dto.PaymentRequestDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@Data
public class InventoryEvent implements Event {

    private UUID eventId = UUID.randomUUID();
    private Date eventDate = new Date();
    private OrderRequestDto orderRequestDto; // Carrying order info
    private InventoryStatus inventoryStatus;

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Date getDate() {
        return eventDate;
    }

    public InventoryEvent(OrderRequestDto orderRequestDto, InventoryStatus inventoryStatus) {
        this.orderRequestDto = orderRequestDto;
        this.inventoryStatus = inventoryStatus;
    }
}
