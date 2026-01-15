package com.cdac.hpuat.issuetopatient.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class IssueRequestDto {
    private Integer hstnumStoreId; // storeId
    private Integer gnumHospitalCode; // hospitalCode
    private String hrgnumPuk; // puk
    private String hststrRecieveBy; // receivingPersonName
    private String gstrRemarks; // remarks
    private Integer sstnumReqtypeId; // reqTypeId
    private Integer sstnumItemCatNo; // itemCatNo
    private Integer gnumDeptCode; // deptCode
    private Integer gnumSeatid; // seatId

    // Patient Details
    private String hststrPatientName; // patientName
    private Date hstdtAge; // patientAge
    private Integer gnumGenderCode; // patientGenderCode

    private List<IssueItemDto> items;
}
