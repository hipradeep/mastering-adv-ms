package com.cdac.hpuat.orchestrator.service;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cdac.hpuat.orchestrator.dto.IssueRequestDto;
import com.cdac.hpuat.orchestrator.dto.command.ReserveStockCommand;
import com.cdac.hpuat.orchestrator.repository.SagaTransactionRepository;
import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;
import com.cdac.hpuat.orchestrator.enums.TransactionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.cdac.hpuat.orchestrator.config.KafkaConfigProperties.TOPIC_INVENTORY_COMMANDS;

import org.springframework.transaction.annotation.Transactional;

@Service
public class OrchestratorService {

    @Autowired
    private KafkaProducerService producerService;

    @Autowired
    private SagaTransactionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public String initiateIssueTransaction(IssueRequestDto requestDto) {
        // 1. Generate unique Transaction ID
        String transactionId = UUID.randomUUID().toString();

        try {
            // 2. Persist State to DB
            HsttSagaTransaction transaction = new HsttSagaTransaction();
            transaction.setHststrTransactionId(transactionId);
            transaction.setHststrStatus(TransactionStatus.STARTED);

            // Populate new columns
            transaction.setGnumHospitalCode(requestDto.getGnumHospitalCode());
            transaction.setHstnumStoreId(requestDto.getHstnumStoreId());
            transaction.setHststrProcessType("ISSUE_TO_PATIENT");
            transaction.setHststrIpAddress(requestDto.getHststrIpAddress());
            transaction.setHstnumSeatId(requestDto.getHstnumSeatId());
            transaction.setHststrUserId(requestDto.getHststrUserId());

            transaction.setHststrRequestPayload(objectMapper.writeValueAsString(requestDto));
            repository.save(transaction);
        } catch (Exception e) {
            throw new RuntimeException("Failed to persist transaction", e);
        }

        // 3. Create ReserveStockCommand
        ReserveStockCommand command = new ReserveStockCommand(
                transactionId,
                requestDto.getGnumHospitalCode(),
                requestDto.getHstnumStoreId(),
                requestDto.getItems());

        // 4. Send to Inventory Service (Step 1 of Saga)
        // Topic: inventory.commands, Key: transactionId (Partition Key)
        producerService.sendMessage(TOPIC_INVENTORY_COMMANDS, transactionId, command);

        return transactionId;
    }
}
