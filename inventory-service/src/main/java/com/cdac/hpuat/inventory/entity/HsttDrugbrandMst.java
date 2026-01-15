package com.cdac.hpuat.inventory.entity;

import java.io.Serializable;
import java.util.Date;

import com.cdac.hpuat.inventory.entity.entityids.HsttDrugbrandMstPK;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hstt_drugbrand_mst")
@IdClass(HsttDrugbrandMstPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttDrugbrandMst implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "hstnum_itembrand_id")
    private Integer hstnumItembrandId;

    @Id
    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Column(name = "hstnum_item_id")
    private Integer hstnumItemId;

    @Column(name = "sstnum_item_cat_no")
    private Integer sstnumItemCatNo;

    @Column(name = "hststr_item_name")
    private String hststrItemName;

    @Column(name = "hstnum_manufacturer_id")
    private Integer hstnumManufacturerId;

    @Column(name = "hstnum_default_rate")
    private Double hstnumDefaultRate;

    @Column(name = "hstnum_rate_unit_id")
    private Integer hstnumRateUnitId;

    @Column(name = "hstnum_approved_type")
    private Integer hstnumApprovedType;

    @Column(name = "hstnum_issue_type")
    private Integer hstnumIssueType;

    @Column(name = "hststr_specification")
    private String hststrSpecification;

    @Column(name = "hstnum_item_make")
    private Integer hstnumItemMake;

    @Column(name = "gstr_remarks")
    private String gstrRemarks;

    @Column(name = "gdt_effective_frm")
    private Date gdtEffectiveFrm;

    @Column(name = "gnum_lstmod_seatid")
    private Integer gnumLstmodSeatid;

    @Column(name = "gdt_lstmod_date")
    private Date gdtLstmodDate;

    @Column(name = "hstnum_currpo_no")
    private String hstnumCurrpoNo;

    @Column(name = "gdt_entry_date")
    private Date gdtEntryDate;

    @Column(name = "gnum_seatid")
    private Integer gnumSeatid;

    @Column(name = "hstdt_currpo_date")
    private Date hstdtCurrpoDate;

    @Column(name = "hstnum_currpur_rate")
    private Double hstnumCurrpurRate;

    @Column(name = "gnum_isvalid")
    private Integer gnumIsvalid;

    @Column(name = "hstnum_currpur_rate_unitid")
    private Integer hstnumCurrpurRateUnitid;

    @Column(name = "hstnum_currsupplier_id")
    private Integer hstnumCurrsupplierId;

    @Column(name = "hstnum_lstpo_no")
    private String hstnumLstpoNo;

    @Column(name = "hstdt_lstpo_date")
    private Date hstdtLstpoDate;

    @Column(name = "hstnum_lstpur_rate")
    private Double hstnumLstpurRate;

    @Column(name = "hstnum_lstpur_rate_unitid")
    private Integer hstnumLstpurRateUnitid;

    @Column(name = "hstnum_lstrec_qty")
    private Double hstnumLstrecQty;

    @Column(name = "hstnum_lstrec_qty_unitid")
    private Integer hstnumLstrecQtyUnitid;

    @Column(name = "hstnum_lstsupplier_id")
    private Integer hstnumLstsupplierId;

    @Column(name = "hstnum_set_sachet_flag")
    private Integer hstnumSetSachetFlag;

    @Column(name = "hstnum_itemtype_id")
    private Integer hstnumItemtypeId;

    @Column(name = "hstnum_is_quantified")
    private Integer hstnumIsQuantified;

    @Column(name = "hstnum_lstrec_date")
    private Date hstnumLstrecDate;

    @Column(name = "hstnum_lstinvoice_no")
    private String hstnumLstinvoiceNo;

    @Column(name = "hstnum_lstinvoice_date")
    private Date hstnumLstinvoiceDate;

    @Column(name = "hststr_cpa_code")
    private String hststrCpaCode;

    @Column(name = "hstnum_brand_reserve_flag")
    private Integer hstnumBrandReserveFlag;

    @Column(name = "hstnum_issuerate_value")
    private Double hstnumIssuerateValue;

    @Column(name = "hstnum_qc_type")
    private Integer hstnumQcType;

    @Column(name = "hstnum_grp_id")
    private Integer hstnumGrpId;

    @Column(name = "hstnum_subgrp_id")
    private Integer hstnumSubgrpId;

    @Column(name = "hstnum_market_rate")
    private Double hstnumMarketRate;

    @Column(name = "hstnum_market_rate_unit_id")
    private Integer hstnumMarketRateUnitId;

    @Column(name = "hstnum_inventory_unitid")
    private Integer hstnumInventoryUnitid;

    @Column(name = "hstnum_display_order")
    private Integer hstnumDisplayOrder;

    @Column(name = "hstnum_default_unitid")
    private Integer hstnumDefaultUnitid;

    @Column(name = "hstnum_batchno_req")
    private Integer hstnumBatchnoReq;

    @Column(name = "hstnum_expirydate_req")
    private Integer hstnumExpirydateReq;

    @Column(name = "hstnum_subcatgtype_id")
    private Integer hstnumSubcatgtypeId;

    @Column(name = "hstnum_ved_category")
    private Integer hstnumVedCategory;

    @Column(name = "hstnum_drug_class_code")
    private Integer hstnumDrugClassCode;

    @Column(name = "hstnum_sample_send_limit")
    private Integer hstnumSampleSendLimit;

    @Column(name = "hstnum_default_rate_in_base")
    private Double hstnumDefaultRateInBase;

    @Column(name = "hststr_moving_type_id")
    private String hststrMovingTypeId;

    @Column(name = "sstnum_budget_class_id")
    private Integer sstnumBudgetClassId;

    @Column(name = "sstnum_certificate_id")
    private Integer sstnumCertificateId;

    @Column(name = "sstnum_drug_standard_id")
    private Integer sstnumDrugStandardId;

    @Column(name = "hstnum_edl_flag")
    private Integer hstnumEdlFlag;

    @Column(name = "hstnum_category_id")
    private Integer hstnumCategoryId;

    @Column(name = "hststr_strength")
    private String hststrStrength;

    @Column(name = "hststr_drug_short_name")
    private String hststrDrugShortName;

    @Column(name = "rate_unit")
    private Integer rateUnit;

    @Column(name = "rate")
    private Double rate;

    @Column(name = "hstnum_inventory_unitid_old")
    private Integer hstnumInventoryUnitidOld;

    @Column(name = "hststr_iphs_name")
    private String hststrIphsName;

    @Column(name = "hstnum_iphs_flag")
    private Integer hstnumIphsFlag;

    @Column(name = "approvetype")
    private String approvetype;

    @Column(name = "budgetclassname")
    private String budgetclassname;

    @Column(name = "certificatename")
    private String certificatename;

    @Column(name = "drugtype")
    private String drugtype;

    @Column(name = "genericdrugname")
    private String genericdrugname;

    @Column(name = "hstnum_drug_for")
    private Integer hstnumDrugFor;

    @Column(name = "hstnum_iphs_drug")
    private Integer hstnumIphsDrug;

    @Column(name = "hstnum_packing_unitid")
    private Integer hstnumPackingUnitid;

    @Column(name = "hststr_iphs_item_name")
    private String hststrIphsItemName;

    @Column(name = "manufacturername")
    private String manufacturername;

    @Column(name = "subcategoryname")
    private String subcategoryname;
}
