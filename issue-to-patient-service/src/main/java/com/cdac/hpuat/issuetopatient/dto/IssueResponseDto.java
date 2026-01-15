package com.cdac.hpuat.issuetopatient.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class IssueResponseDto {
    private Integer hstnumStoreId;
    private Integer hstnumIssueNo;
    private Integer gnumHospitalCode;
    private String hrgnumPuk;
    private Integer sstnumReqtypeId;
    private Integer sstnumItemCatNo;
    private Date hstdtIssueDate;
    private Integer gnumDeptCode;
    private String hststrRecieveBy;
    private String gstrRemarks;
    private Integer gnumSeatid;

    private String hststrPatientName;
    private Date hstdtAge;
    private Integer gnumGenderCode;

    private List<IssueItemResponseDto> items;
}
