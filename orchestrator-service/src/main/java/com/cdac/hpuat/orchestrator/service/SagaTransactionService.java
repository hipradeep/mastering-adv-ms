package com.cdac.hpuat.orchestrator.service;

import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;

public interface SagaTransactionService {
    HsttSagaTransaction getTransactionStatus(String transactionId);
}
