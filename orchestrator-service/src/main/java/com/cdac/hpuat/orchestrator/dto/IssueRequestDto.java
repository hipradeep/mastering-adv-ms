package com.cdac.hpuat.orchestrator.dto;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class IssueRequestDto implements Serializable {
    private Integer hstnumStoreId;
    private String hrgnumPuk;
    private String hststrPatientName;
    private String gstrRemarks;
    private Integer gnumHospitalCode;
    private String hststrIpAddress;
    private Long hstnumSeatId;
    private String hststrUserId;
    private List<IssueItemDto> items;
}
