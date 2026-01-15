package com.cdac.hpuat.orchestrator.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import com.cdac.hpuat.orchestrator.enums.TransactionStatus;

@Entity
@Table(name = "hstt_saga_transactions", schema = "saga")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttSagaTransaction {

    @Id
    @Column(name = "hststr_transaction_id")
    private String hststrTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "hststr_status")
    private TransactionStatus hststrStatus;

    @Column(name = "hststr_request_payload", columnDefinition = "TEXT")
    private String hststrRequestPayload; // JSON representation of IssueRequestDto

    @Column(name = "hststr_failure_reason")
    private String hststrFailureReason;

    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;

    @Column(name = "hstnum_store_id")
    private Integer hstnumStoreId;

    @Column(name = "hststr_process_type")
    private String hststrProcessType;

    @Column(name = "hststr_ip_address")
    private String hststrIpAddress;

    @Column(name = "hstnum_seat_id")
    private Long hstnumSeatId;

    @Column(name = "hststr_user_id")
    private String hststrUserId;

    @Column(name = "hstdt_completion_date")
    private LocalDateTime hstdtCompletionDate;

    @Column(name = "gdt_entry_date")
    private LocalDateTime gdtEntryDate;

    @Column(name = "gdt_lstmod_date")
    private LocalDateTime gdtLstmodDate;

    @PrePersist
    protected void onCreate() {
        gdtEntryDate = LocalDateTime.now();
        gdtLstmodDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        gdtLstmodDate = LocalDateTime.now();
    }
}
