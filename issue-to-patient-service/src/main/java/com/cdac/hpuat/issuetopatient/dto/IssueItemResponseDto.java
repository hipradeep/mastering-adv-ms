package com.cdac.hpuat.issuetopatient.dto;

import java.util.Date;
import lombok.Data;

@Data
public class IssueItemResponseDto {
    private Integer hstnumItembrandId;
    private Integer hstnumItemId;
    private String hststrBatchSlNo;
    private Double hstnumIssueQty;
    private Integer hstnumIssueqtyUnitid;
    private Double hstnumRate;
    private Integer hstnumRateUnitid;
    private Integer hstnumMfgId;
    private Date hstdtExpiryDate;
    private Integer hstnumStockStatusCode;
    private Integer hstnumGroupId;
    private Integer hstnumSubgroupId;
    private String hststrItemSlNo;
    private Integer hstnumConsumableFlag;
    private Integer hstnumIssueNo;
    private Date hstdtIssueDate;
    private Date gdtEntryDate;
    private String gstrRemarks;
    private Integer hstnumStoreId;
}
