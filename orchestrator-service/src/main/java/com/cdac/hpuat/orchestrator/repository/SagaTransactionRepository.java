package com.cdac.hpuat.orchestrator.repository;

import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.hpuat.orchestrator.enums.TransactionStatus;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SagaTransactionRepository extends JpaRepository<HsttSagaTransaction, String> {
    List<HsttSagaTransaction> findByHststrStatusAndGdtEntryDateBefore(TransactionStatus status,
            LocalDateTime timestamp);
}
