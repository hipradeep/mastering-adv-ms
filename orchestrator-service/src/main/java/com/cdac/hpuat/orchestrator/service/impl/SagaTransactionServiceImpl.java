package com.cdac.hpuat.orchestrator.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;
import com.cdac.hpuat.orchestrator.exception.ResourceNotFoundException;
import com.cdac.hpuat.orchestrator.repository.SagaTransactionRepository;
import com.cdac.hpuat.orchestrator.service.SagaTransactionService;

@Service
public class SagaTransactionServiceImpl implements SagaTransactionService {

    @Autowired
    private SagaTransactionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public HsttSagaTransaction getTransactionStatus(String transactionId) {
        return repository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for ID: " + transactionId));
    }
}
