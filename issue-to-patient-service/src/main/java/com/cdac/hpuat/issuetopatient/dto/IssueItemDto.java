package com.cdac.hpuat.issuetopatient.dto;

import java.io.Serializable;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class IssueItemDto implements Serializable {
    private Integer hstnumItembrandId;
    private String hststrBatchSlNo;
    private Double hstnumIssueQty;
    private Integer hstnumStoreId;
}
