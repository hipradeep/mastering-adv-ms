package com.cdac.hpuat.issuetopatient.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssueCreatedEvent {
    private String transactionId;
    private boolean success;
    private String message;
}
