package com.cdac.hpuat.orchestrator.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmStockCommand {
    private String transactionId;
    private boolean commit; // true=commit, false=rollback (if using same command for both)
}
