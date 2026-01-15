package com.cdac.hpuat.inventory.entity;

import java.io.Serializable;
import java.util.Date;

import com.cdac.hpuat.inventory.entity.entityids.HsttDrugMstPK;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hstt_drug_mst")
@IdClass(HsttDrugMstPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttDrugMst implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "hstnum_item_id")
    private Integer hstnumItemId;

    @Id
    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Column(name = "hstnum_group_id")
    private Integer hstnumGroupId;

    @Column(name = "hstnum_subgroup_id")
    private Integer hstnumSubgroupId;

    @Column(name = "sstnum_item_cat_no")
    private Integer sstnumItemCatNo;

    @Column(name = "hststr_item_name")
    private String hststrItemName;

    @Column(name = "hstnum_batchno_req")
    private Integer hstnumBatchnoReq;

    @Column(name = "hstnum_expirydate_req")
    private Integer hstnumExpirydateReq;

    @Column(name = "hstnum_shelflife")
    private Integer hstnumShelflife;

    @Column(name = "hstnum_shelflife_unit")
    private Integer hstnumShelflifeUnit;

    @Column(name = "gnum_inventory_unitid")
    private Integer gnumInventoryUnitid;

    @Column(name = "hstnum_purchased_leadtime")
    private Integer hstnumPurchasedLeadtime;

    @Column(name = "hstnum_pur_leadtime_unit")
    private Integer hstnumPurLeadtimeUnit;

    @Column(name = "hstnum_consumable_flag")
    private Integer hstnumConsumableFlag;

    @Column(name = "hstnum_is_narcotic")
    private Integer hstnumIsNarcotic;

    @Column(name = "gstr_remarks")
    private String gstrRemarks;

    @Column(name = "gdt_effective_frm")
    private Date gdtEffectiveFrm;

    @Column(name = "gnum_lstmod_seatid")
    private Integer gnumLstmodSeatid;

    @Column(name = "gdt_lstmod_date")
    private Date gdtLstmodDate;

    @Column(name = "gdt_entry_date")
    private Date gdtEntryDate;

    @Column(name = "gnum_seatid")
    private Integer gnumSeatid;

    @Column(name = "gnum_isvalid")
    private Integer gnumIsvalid;

    @Column(name = "hstnum_consent_req")
    private Integer hstnumConsentReq;

    @Column(name = "hststr_cpa_code")
    private String hststrCpaCode;

    @Column(name = "hstnum_pregnancy_safe_flag")
    private Integer hstnumPregnancySafeFlag;

    @Column(name = "hststr_foetus_effects")
    private String hststrFoetusEffects;

    @Column(name = "hstnum_trimester")
    private Integer hstnumTrimester;

    @Column(name = "hstnum_breakable_flag")
    private Integer hstnumBreakableFlag;

    @Column(name = "unit_id_old")
    private Integer unitIdOld;
}
