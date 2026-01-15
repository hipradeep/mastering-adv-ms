package com.cdac.hpuat.orchestrator.dto.command;

import com.cdac.hpuat.orchestrator.dto.IssueItemDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class   ReserveStockCommand {
    private String transactionId;
    private Integer gnumHospitalCode;
    private Integer hstnumStoreId;
    private List<IssueItemDto> items;
}
