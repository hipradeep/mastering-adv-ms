package com.cdac.hpuat.orchestrator.service;

import com.cdac.hpuat.orchestrator.dto.IssueRequestDto;
import com.cdac.hpuat.orchestrator.dto.command.RollbackStockCommand;
import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;
import com.cdac.hpuat.orchestrator.enums.TransactionStatus;
import com.cdac.hpuat.orchestrator.repository.SagaTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.cdac.hpuat.orchestrator.config.KafkaConfigProperties.TOPIC_INVENTORY_COMMANDS;

@Service
public class SagaTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(SagaTimeoutScheduler.class);

    @Autowired
    private SagaTransactionRepository repository;

    @Autowired
    private KafkaProducerService producerService;

    @Autowired
    private ObjectMapper objectMapper;

    // Check every 60 seconds
    @Scheduled(fixedRate = 60000)
    public void checkTimeouts() {
        // Timeout threshold: 5 minutes ago
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);

        // Find transactions stuck in STOCK_CONFIRMED state (waiting for Issue Service)
        List<HsttSagaTransaction> stuckTransactions = repository.findByHststrStatusAndGdtEntryDateBefore(
                TransactionStatus.STOCK_CONFIRMED,
                timeoutThreshold);

        if (!stuckTransactions.isEmpty()) {
            log.info("Found {} stuck transactions. Initiating Rollback...", stuckTransactions.size());
        }

        for (HsttSagaTransaction transaction : stuckTransactions) {
            handleTimeout(transaction);
        }
    }

    private void handleTimeout(HsttSagaTransaction transaction) {
        log.warn("Transaction {} timed out. Rolling back Inventory.", transaction.getHststrTransactionId());

        try {
            // 1. Update Status to TIMED_OUT (to prevent double processing)
            transaction.setHststrStatus(TransactionStatus.TIMED_OUT);
            transaction.setHststrFailureReason("Transaction Timed Out - No response from Issue Service");
            repository.save(transaction);

            // 2. Deserialize Original Request to get item details
            IssueRequestDto originalRequest = objectMapper.readValue(transaction.getHststrRequestPayload(),
                    IssueRequestDto.class);

            // 3. Send Rollback Command to Inventory
            RollbackStockCommand command = new RollbackStockCommand(
                    transaction.getHststrTransactionId(),
                    originalRequest.getGnumHospitalCode(),
                    originalRequest.getHstnumStoreId(),
                    originalRequest.getItems());

            producerService.sendMessage(TOPIC_INVENTORY_COMMANDS, transaction.getHststrTransactionId(), command);

            log.info("Rollback Command sent for Transaction: {}", transaction.getHststrTransactionId());

        } catch (Exception e) {
            log.error("Failed to process timeout for transaction: {}", transaction.getHststrTransactionId(), e);
        }
    }
}
