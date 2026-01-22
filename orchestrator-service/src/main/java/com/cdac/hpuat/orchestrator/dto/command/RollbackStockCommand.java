package com.cdac.hpuat.orchestrator.dto.command;

import com.cdac.hpuat.orchestrator.dto.IssueItemDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RollbackStockCommand {
    private String transactionId;
    private Integer gnumHospitalCode;
    private Integer hstnumStoreId;
    private List<IssueItemDto> items;
}
