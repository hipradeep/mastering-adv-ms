package com.cdac.hpuat.issuetopatient.entity;

import java.util.Date;

import com.cdac.hpuat.issuetopatient.entity.entityids.HsttPatempIssueDtlPK;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "hstt_patemp_issue_dtl")
@IdClass(HsttPatempIssueDtlPK.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttPatempIssueDtl {

    @Id
    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Id
    @Column(name = "hstnum_issue_no")
    private Integer hstnumIssueNo;

    @Id
    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Column(name = "hrgnum_puk")
    private String hrgnumPuk;

    @Column(name = "sstnum_reqtype_id")
    private Integer sstnumReqtypeId;

    @Column(name = "sstnum_item_cat_no")
    private Integer sstnumItemCatNo;

    @Column(name = "hstdt_issue_date")
    private Date hstdtIssueDate;

    @Column(name = "pisstr_order_by")
    private String pisstrOrderBy;

    @Column(name = "pisdt_order_date")
    private Date pisdtOrderDate;

    @Column(name = "hstnum_net_cost")
    private Double hstnumNetCost;

    @Column(name = "gnum_dept_code")
    private Integer gnumDeptCode;

    @Column(name = "hstdt_financial_start_date")
    private Date hstdtFinancialStartDate;

    @Column(name = "hstdt_financial_end_date")
    private Date hstdtFinancialEndDate;

    @Column(name = "hststr_recieve_by")
    private String hststrRecieveBy;

    @Column(name = "gstr_remarks")
    private String gstrRemarks;

    @Column(name = "gnum_seatid")
    private Integer gnumSeatid;

    @Column(name = "gdt_entry_date")
    private Date gdtEntryDate;

    @Column(name = "gnum_isvalid")
    private Integer gnumIsvalid;

    @Column(name = "hstnum_return_cost")
    private Double hstnumReturnCost;

    @Column(name = "hststr_patient_name")
    private String hststrPatientName;

    @Column(name = "hststr_father_name")
    private String hststrFatherName;

    @Column(name = "hstdt_age")
    private Date hstdtAge;

    @Column(name = "gnum_gender_code")
    private Integer gnumGenderCode;

    @Column(name = "hststr_address")
    private String hststrAddress;

    @Column(name = "hstnum_patient_type")
    private Integer hstnumPatientType;

    @Column(name = "hstnum_service_tax")
    private Double hstnumServiceTax;

    @Column(name = "gstr_hospital_name")
    private String gstrHospitalName;

    @Column(name = "hstnum_out_of_stock_flag")
    private Integer hstnumOutOfStockFlag;

    @Column(name = "hstnum_desktop_issue_no")
    private Integer hstnumDesktopIssueNo;

    @Column(name = "hststr_aadhar_no")
    private String hststrAadharNo;

    @Column(name = "hststr_jeevandayee_no")
    private String hststrJeevandayeeNo;

    @Column(name = "hststr_voter_no")
    private String hststrVoterNo;

    @Column(name = "gnum_diagnosis_code")
    private Integer gnumDiagnosisCode;

    @Column(name = "hstdt_cancel_date")
    private Date hstdtCancelDate;

    @Column(name = "hstnum_cancel_seatid")
    private Integer hstnumCancelSeatid;

    @Column(name = "hststr_cancel_remarks")
    private String hststrCancelRemarks;

    @Column(name = "hstnum_return_flag")
    private Integer hstnumReturnFlag;
}
