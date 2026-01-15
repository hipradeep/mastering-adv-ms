package com.cdac.hpuat.issuetopatient.dto.command;

import com.cdac.hpuat.issuetopatient.dto.IssueItemDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateIssueCommand {
    private String transactionId;
    private Integer gnumHospitalCode;
    private Integer hstnumStoreId;
    private String hrgnumPuk;
    private String hststrPatientName;
    private String gstrRemarks;
    private List<IssueItemDto> items;
}
