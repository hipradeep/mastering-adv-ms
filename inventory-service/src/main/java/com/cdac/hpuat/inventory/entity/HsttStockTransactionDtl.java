package com.cdac.hpuat.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "hstt_stock_transaction_dtl")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttStockTransactionDtl implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hstnum_trans_id")
    private Long hstnumAuditId;

    @Column(name = "hststr_saga_trans_id")
    private String hststrSagaTransId;

    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Column(name = "hstnum_itembrand_id")
    private Integer hstnumItembrandId;

    @Column(name = "hststr_batch_no")
    private String hststrBatchNo;

    @Column(name = "hstnum_transfer_qty")
    private Double hstnumTransferQty;

    @Column(name = "hststr_trans_type")
    private String hststrTransType; // RESERVE, RELEASE

    @Column(name = "hstnum_inhand_qty_before")
    private Double hstnumInhandQtyBefore;

    @Column(name = "hstnum_inhand_qty_after")
    private Double hstnumInhandQtyAfter;

    @Column(name = "hstdt_entry_date")
    private LocalDateTime hstdtEntryDate;

    @Column(name = "hststr_remarks")
    private String hststrRemarks;

    @PrePersist
    public void onCreate() {
        this.hstdtEntryDate = LocalDateTime.now();
    }
}
