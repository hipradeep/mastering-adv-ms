package com.cdac.hpuat.issuetopatient.entity.entityids;

import java.io.Serializable;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttPatempIssueItemDtlPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Column(name = "hstnum_issue_no")
    private Integer hstnumIssueNo;

    @Column(name = "hstnum_itembrand_id")
    private Integer hstnumItembrandId;

    @Column(name = "hststr_batch_sl_no")
    private String hststrBatchSlNo;

    @Column(name = "hstnum_mfg_id")
    private Integer hstnumMfgId;

    @Column(name = "hstnum_programme_id")
    private Integer hstnumProgrammeId;

    @Column(name = "hstnum_stock_status_code")
    private Integer hstnumStockStatusCode;

    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;
}
