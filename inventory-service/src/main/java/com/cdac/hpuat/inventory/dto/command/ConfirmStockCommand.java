package com.cdac.hpuat.inventory.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmStockCommand {
    private String transactionId;
    private boolean commit;
}
