package com.cdac.hpuat.orchestrator.repository;

import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SagaTransactionRepository extends JpaRepository<HsttSagaTransaction, String> {
}
