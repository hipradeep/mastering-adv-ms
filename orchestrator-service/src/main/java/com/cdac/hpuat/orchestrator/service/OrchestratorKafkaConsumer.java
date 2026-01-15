package com.cdac.hpuat.orchestrator.service;

import com.cdac.hpuat.orchestrator.dto.command.ConfirmStockCommand;
import com.cdac.hpuat.orchestrator.dto.event.IssueCreatedEvent;
import com.cdac.hpuat.orchestrator.repository.SagaTransactionRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.cdac.hpuat.orchestrator.dto.event.StockReservedEvent;
import com.cdac.hpuat.orchestrator.dto.command.CreateIssueCommand;
import com.cdac.hpuat.orchestrator.dto.IssueRequestDto;
import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;
import com.cdac.hpuat.orchestrator.enums.TransactionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

import static com.cdac.hpuat.orchestrator.config.KafkaConfigProperties.*;

@Service
public class OrchestratorKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorKafkaConsumer.class);

    @Autowired
    private KafkaProducerService producerService;

    @Autowired
    private SagaTransactionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @KafkaListener(topics = TOPIC_ORCHESTRATOR_REPLIES, groupId = GROUP_ID_ORCHESTRATOR)
    public void consume(ConsumerRecord<?, ?> record) {
        Object message = record.value();
        log.info("Received Reply: {}", message);
        try {
            if (message instanceof StockReservedEvent) {
                handleStockReservedBox((StockReservedEvent) message);
            } else if (message instanceof IssueCreatedEvent) {
                handleIssueCreatedBox((IssueCreatedEvent) message);
            } else {
                log.warn("Unknown message type: {}", message.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing reply", e);
        }
    }

    private void handleStockReservedBox(StockReservedEvent event) throws Exception {
        log.info("Stock Reserved Event: {}", event);

        HsttSagaTransaction transaction = repository.findById(event.getTransactionId()).orElse(null);
        if (transaction == null) {
            log.error("Transaction not found for ID: {}", event.getTransactionId());
            return;
        }

        if (event.isSuccess()) {
            IssueRequestDto originalRequest = objectMapper.readValue(transaction.getHststrRequestPayload(),
                    IssueRequestDto.class);

            // Update Status
            transaction.setHststrStatus(TransactionStatus.STOCK_CONFIRMED);
            repository.save(transaction);

            // Proceed to Step 2: Create Issue
            CreateIssueCommand command = new CreateIssueCommand(
                    event.getTransactionId(),
                    originalRequest.getGnumHospitalCode(),
                    originalRequest.getHstnumStoreId(),
                    originalRequest.getHrgnumPuk(),
                    originalRequest.getHststrPatientName(),
                    originalRequest.getGstrRemarks(),
                    originalRequest.getItems());

            producerService.sendMessage(TOPIC_ISSUE_COMMANDS, event.getTransactionId(), command);
        } else {
            log.warn("Stock Reservation Failed for Transaction ID: {}. Saga Aborted.", event.getTransactionId());
            transaction.setHststrStatus(TransactionStatus.STOCK_FAILED);
            transaction.setHststrFailureReason("Stock Reservation Failed");
            repository.save(transaction);
        }
    }

    private void handleIssueCreatedBox(IssueCreatedEvent event) throws Exception {
        log.info("Issue Created Event: {}", event);

        HsttSagaTransaction transaction = repository.findById(event.getTransactionId()).orElse(null);
        if (transaction == null) {
            log.error("Transaction not found for ID: {}", event.getTransactionId());
            return;
        }

        if (event.isSuccess()) {
            // Step 3: Confirm Stock
            ConfirmStockCommand command = new ConfirmStockCommand(event.getTransactionId(), true);

            producerService.sendMessage(TOPIC_INVENTORY_COMMANDS, event.getTransactionId(), command);

            log.info("Saga Completed Successfully for Transaction: {}", event.getTransactionId());
            transaction.setHststrStatus(TransactionStatus.COMPLETED);
            transaction.setHstdtCompletionDate(LocalDateTime.now());
            repository.save(transaction);

        } else {
            // Rollback Stock
            log.warn("Issue Creation Failed. Rolling back Stock for Transaction: {}", event.getTransactionId());
            ConfirmStockCommand command = new ConfirmStockCommand(
                    event.getTransactionId(),
                    false // Rollback
            );
            producerService.sendMessage(
                    TOPIC_INVENTORY_COMMANDS,
                    event.getTransactionId(), command);

            transaction.setHststrStatus(TransactionStatus.ISSUE_CREATION_FAILED);
            transaction.setHststrFailureReason("Issue Creation Failed");
            repository.save(transaction);
        }
    }
}
