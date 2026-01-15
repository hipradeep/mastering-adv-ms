package com.cdac.hpuat.inventory.dto;

import lombok.Data;

@Data
public class DrugBrandResponseDto {
    private Integer hstnumItembrandId;
    private Integer gnumHospitalCode;
    private Integer hstnumItemId;
    private String hststrItemName;
    private Integer hstnumManufacturerId;
    private String hststrDrugShortName;
    private Double hstnumDefaultRate;
    private Integer hstnumRateUnitId;
}
