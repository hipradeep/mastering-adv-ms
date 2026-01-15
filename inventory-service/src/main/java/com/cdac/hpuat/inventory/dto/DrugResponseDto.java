package com.cdac.hpuat.inventory.dto;

import lombok.Data;

@Data
public class DrugResponseDto {
    private Integer hstnumItemId;
    private Integer gnumHospitalCode;
    private String hststrItemName;
    private Integer hstnumGroupId;
    private Integer hstnumSubgroupId;
    private Integer sstnumItemCatNo;
    private String hststrStrength;
    private Integer hstnumConsumableFlag;
    private Integer hstnumIsNarcotic;
}
