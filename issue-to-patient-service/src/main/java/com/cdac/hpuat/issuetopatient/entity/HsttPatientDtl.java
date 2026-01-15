package com.cdac.hpuat.issuetopatient.entity;

import java.util.Date;
import com.cdac.hpuat.issuetopatient.entity.entityids.HsttPatientDtlPK;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "hstt_patient_dtl")
@IdClass(HsttPatientDtlPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttPatientDtl {

    @Id
    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Id
    @Column(name = "hststr_cr_no")
    private String hststrCrNo;

    @Column(name = "hststr_patient_name")
    private String hststrPatientName;

    @Column(name = "hststr_father_name")
    private String hststrFatherName;

    @Column(name = "hstdt_age")
    private Date hstdtAge;

    @Column(name = "hstnum_gender_code")
    private Integer hstnumGenderCode;

    @Column(name = "hststr_adhar_no")
    private String hststrAdharNo;

    @Column(name = "hststr_jeevan_dayni_no")
    private String hststrJeevanDayniNo;

    @Column(name = "hststr_voter_id")
    private String hststrVoterId;

    @Column(name = "gnum_seatid")
    private Integer gnumSeatid;

    @Column(name = "gdt_last_updated_date")
    private Date gdtLastUpdatedDate;

    @Column(name = "gnum_last_updated_seatid")
    private Integer gnumLastUpdatedSeatid;

    @Column(name = "gdt_entry_date")
    private Date gdtEntryDate;
}
