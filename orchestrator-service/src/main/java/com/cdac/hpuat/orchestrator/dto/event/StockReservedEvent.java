package com.cdac.hpuat.orchestrator.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockReservedEvent {
    private String transactionId;
    private boolean success;
    private String message;
}
