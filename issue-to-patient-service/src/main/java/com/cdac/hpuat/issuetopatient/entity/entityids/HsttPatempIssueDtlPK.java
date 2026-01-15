package com.cdac.hpuat.issuetopatient.entity.entityids;

import java.io.Serializable;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttPatempIssueDtlPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Column(name = "hstnum_issue_no")
    private Integer hstnumIssueNo;

    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;
}
