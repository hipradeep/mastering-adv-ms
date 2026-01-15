package com.cdac.hpuat.inventory.entity;

import java.io.Serializable;
import java.util.Date;

import com.cdac.hpuat.inventory.entity.entityids.HsttDrugCurrstockDtlPK;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hstt_drug_currstock_dtl")
@IdClass(HsttDrugCurrstockDtlPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttDrugCurrstockDtl implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Id
    @Column(name = "hstnum_itembrand_id")
    private Integer hstnumItembrandId;

    @Id
    @Column(name = "hststr_batch_no")
    private String hststrBatchNo;

    @Id
    @Column(name = "hstnum_mfg_id")
    private Integer hstnumMfgId;

    @Id
    @Column(name = "hstnum_programme_id")
    private Integer hstnumProgrammeId;

    @Id
    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Id
    @Column(name = "hstnum_stock_status_code")
    private Integer hstnumStockStatusCode;

    @Column(name = "hstnum_item_id")
    private Integer hstnumItemId;

    @Column(name = "sstnum_item_cat_no")
    private Integer sstnumItemCatNo;

    @Column(name = "hstnum_group_id")
    private Integer hstnumGroupId;

    @Column(name = "hstnum_subgroup_id")
    private Integer hstnumSubgroupId;

    @Column(name = "hstdt_expiry_date")
    private Date hstdtExpiryDate;

    @Column(name = "hstdt_manuf_date")
    private Date hstdtManufDate;

    @Column(name = "hstnum_inventory_flag")
    private Integer hstnumInventoryFlag;

    @Column(name = "hstnum_inhand_qty")
    private Double hstnumInhandQty;

    @Column(name = "hstnum_inhand_qty_unitid")
    private Integer hstnumInhandQtyUnitid;

    @Column(name = "hstnum_supplier_id")
    private Integer hstnumSupplierId;

    @Column(name = "hstnum_rate")
    private Double hstnumRate;

    @Column(name = "hstnum_rate_unitid")
    private Integer hstnumRateUnitid;

    @Column(name = "hstnum_saleprice")
    private Double hstnumSaleprice;

    @Column(name = "hstnum_saleprice_unitid")
    private Integer hstnumSalepriceUnitid;

    @Column(name = "hstnum_po_no")
    private String hstnumPoNo;

    @Column(name = "gnum_lstmod_seatid")
    private Integer gnumLstmodSeatid;

    @Column(name = "gdt_lstmod_date")
    private Date gdtLstmodDate;

    @Column(name = "gnum_seatid")
    private Integer gnumSeatid;

    @Column(name = "gdt_entry_date")
    private Date gdtEntryDate;

    @Column(name = "gnum_isvalid")
    private Integer gnumIsvalid;

    @Column(name = "hstnum_reserved_qty")
    private Double hstnumReservedQty;

    @Column(name = "hstnum_blocked_qty")
    private Double hstnumBlockedQty;

    @Column(name = "hstnum_blockedqty_unitid")
    private Integer hstnumBlockedqtyUnitid;

    @Column(name = "hstdt_po_date")
    private Date hstdtPoDate;

    @Column(name = "hstdt_recieved_date")
    private Date hstdtRecievedDate;

    @Column(name = "hstnum_currency_id")
    private Integer hstnumCurrencyId;

    @Column(name = "hstnum_freeitem_flag")
    private Integer hstnumFreeitemFlag;

    @Column(name = "hstnum_current_value")
    private Double hstnumCurrentValue;

    @Column(name = "hstnum_blocked_issueqty")
    private Double hstnumBlockedIssueqty;

    @Column(name = "hstnum_blocked_issueqty_unitid")
    private Integer hstnumBlockedIssueqtyUnitid;

    @Column(name = "hstnum_opbalance_qty")
    private Double hstnumOpbalanceQty;

    @Column(name = "hstnum_issue_recieve_flag")
    private Integer hstnumIssueRecieveFlag;

    @Column(name = "hstnum_invoice_no")
    private String hstnumInvoiceNo;

    @Column(name = "hstnum_invoice_date")
    private Date hstnumInvoiceDate;

    @Column(name = "hststr_specification")
    private String hststrSpecification;

    @Column(name = "hststr_rack_no")
    private String hststrRackNo;

    @Column(name = "hstnum_qc_issue_flag")
    private Integer hstnumQcIssueFlag;

    @Column(name = "hstdt_qc_date")
    private Date hstdtQcDate;

    @Column(name = "hststr_tender_no")
    private String hststrTenderNo;

    @Column(name = "hstnum_is_dwh")
    private Integer hstnumIsDwh;

    @Column(name = "hstnum_month_qty0")
    private Double hstnumMonthQty0;

    @Column(name = "hstnum_month_qty1")
    private Double hstnumMonthQty1;

    @Column(name = "hstnum_store_level")
    private Integer hstnumStoreLevel;

    @Column(name = "hstnum_rate_base_value")
    private Double hstnumRateBaseValue;

    @Column(name = "hstnum_salerate_base_value")
    private Double hstnumSalerateBaseValue;

    @Column(name = "sstnum_dwh_type_id")
    private Integer sstnumDwhTypeId;

    @Column(name = "num_dist_id")
    private Integer numDistId;

    @Column(name = "hstnum_parent_store_id")
    private Integer hstnumParentStoreId;

    @Column(name = "hstnum_is_edl")
    private Integer hstnumIsEdl;

    @Column(name = "hstnum_expired_qty")
    private Double hstnumExpiredQty;

    @Column(name = "hststr_trans_no")
    private String hststrTransNo;

    @Column(name = "hstdt_document_date")
    private Date hstdtDocumentDate;

    @Column(name = "sstnum_reqtype_id")
    private Integer sstnumReqtypeId;

    @Column(name = "hststr_particulars")
    private String hststrParticulars;

    @Column(name = "hstnum_inhand_qty_unitid_old")
    private Integer hstnumInhandQtyUnitidOld;

    @Column(name = "rate_base_value_tmp")
    private Double rateBaseValueTmp;

    @Column(name = "hstnum_duplicate_drugreq_flag")
    private Integer hstnumDuplicateDrugreqFlag;

}
