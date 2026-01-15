package com.cdac.hpuat.inventory.entity.entityids;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttDrugCurrstockDtlPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Column(name = "hstnum_itembrand_id")
    private Integer hstnumItembrandId;

    @Column(name = "hststr_batch_no")
    private String hststrBatchNo;

    @Column(name = "hstnum_mfg_id")
    private Integer hstnumMfgId;

    @Column(name = "hstnum_programme_id")
    private Integer hstnumProgrammeId;

    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Column(name = "hstnum_stock_status_code")
    private Integer hstnumStockStatusCode;
}
