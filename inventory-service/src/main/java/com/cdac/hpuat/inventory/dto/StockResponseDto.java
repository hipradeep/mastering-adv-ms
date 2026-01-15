package com.cdac.hpuat.inventory.dto;

import java.util.Date;
import lombok.Data;

@Data
public class StockResponseDto {
    private Integer hstnumStoreId;
    private Integer hstnumItembrandId;
    private String hststrBatchNo;
    private Integer hstnumMfgId;
    private Integer hstnumProgrammeId;
    private Integer gnumHospitalCode;
    private Integer hstnumStockStatusCode;
    private Integer hstnumItemId;
    private Double hstnumInhandQty;
    private Integer hstnumInhandQtyUnitid;
    private Double hstnumRate;
    private Integer hstnumRateUnitid;
    private Double hstnumSaleprice;
    private Integer hstnumSalepriceUnitid;
    private Date hstdtExpiryDate;
    private Date hstdtManufDate;
}
