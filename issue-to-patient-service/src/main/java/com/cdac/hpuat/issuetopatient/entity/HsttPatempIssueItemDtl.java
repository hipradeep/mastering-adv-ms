package com.cdac.hpuat.issuetopatient.entity;

import java.util.Date;

import com.cdac.hpuat.issuetopatient.entity.entityids.HsttPatempIssueItemDtlPK;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "hstt_patemp_issue_item_dtl")
@IdClass(HsttPatempIssueItemDtlPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttPatempIssueItemDtl {

    @Id
    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Id
    @Column(name = "hstnum_issue_no")
    private Integer hstnumIssueNo;

    @Id
    @Column(name = "hstnum_itembrand_id")
    private Integer hstnumItembrandId;

    @Id
    @Column(name = "hststr_batch_sl_no")
    private String hststrBatchSlNo;

    @Id
    @Column(name = "hstnum_mfg_id")
    private Integer hstnumMfgId;

    @Id
    @Column(name = "hstnum_programme_id")
    private Integer hstnumProgrammeId;

    @Id
    @Column(name = "hstnum_stock_status_code")
    private Integer hstnumStockStatusCode;

    @Id
    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Column(name = "hstnum_item_id")
    private Integer hstnumItemId;

    @Column(name = "hststr_item_sl_no")
    private String hststrItemSlNo;

    @Column(name = "hstdt_issue_date")
    private Date hstdtIssueDate;

    @Column(name = "hstnum_group_id")
    private Integer hstnumGroupId;

    @Column(name = "hstnum_subgroup_id")
    private Integer hstnumSubgroupId;

    @Column(name = "hstnum_rate")
    private Double hstnumRate;

    @Column(name = "hstnum_rate_unitid")
    private Integer hstnumRateUnitid;

    @Column(name = "hstnum_issue_qty")
    private Double hstnumIssueQty;

    @Column(name = "hstnum_issueqty_unitid")
    private Integer hstnumIssueqtyUnitid;

    @Column(name = "hstnum_inhand_qty")
    private Double hstnumInhandQty;

    @Column(name = "hstnum_inhand_qty_unitid")
    private Integer hstnumInhandQtyUnitid;

    @Column(name = "hstdt_manuf_date")
    private Date hstdtManufDate;

    @Column(name = "hstdt_expiry_date")
    private Date hstdtExpiryDate;

    @Column(name = "hstnum_consumable_flag")
    private Integer hstnumConsumableFlag;

    @Column(name = "gstr_remarks")
    private String gstrRemarks;

    @Column(name = "gnum_isvalid")
    private Integer gnumIsvalid;

    @Column(name = "hstnum_return_qty")
    private Double hstnumReturnQty;

    @Column(name = "hstnum_retqty_unitid")
    private Integer hstnumRetqtyUnitid;

    @Column(name = "hstnum_total_cost")
    private Double hstnumTotalCost;

    @Column(name = "hstnum_total_return_cost")
    private Double hstnumTotalReturnCost;

    @Column(name = "hrgnum_puk")
    private String hrgnumPuk;

    @Column(name = "gdt_entry_date")
    private Date gdtEntryDate;

    @Column(name = "hstnum_sale_rate")
    private Double hstnumSaleRate;
}
