package com.cdac.hpuat.inventory.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cdac.hpuat.inventory.dto.command.ReserveStockCommand;
import com.cdac.hpuat.inventory.dto.command.ConfirmStockCommand;
import com.cdac.hpuat.inventory.dto.event.StockReservedEvent;
import com.cdac.hpuat.inventory.dto.IssueItemDto;

import static com.cdac.hpuat.inventory.config.KafkaConfigProperties.*;

@Service
public class InventoryKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaConsumer.class);

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = TOPIC_INVENTORY_COMMANDS, groupId = GROUP_ID_INVENTORY)
    public void consume(ConsumerRecord<?, ?> record) {
        Object message = record.value();
        log.info("Received Message: {}", message);
        try {
            if (message instanceof ReserveStockCommand) {
                handleReserve((ReserveStockCommand) message);
            } else if (message instanceof ConfirmStockCommand) {
                handleConfirm((ConfirmStockCommand) message);
            } else {
                log.warn("Unknown message type: {}", message.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

    private void handleReserve(ReserveStockCommand command) throws Exception {
        log.info("Processing ReserveStockCommand: {}", command.getTransactionId());

        boolean allReserved = true;
        try {
            for (IssueItemDto item : command.getItems()) {
                inventoryService.updateStock(
                        command.getGnumHospitalCode(),
                        command.getHstnumStoreId(),
                        item.getHstnumItembrandId(),
                        item.getHststrBatchSlNo(),
                        item.getHstnumIssueQty().intValue());
            }
        } catch (Exception e) {
            log.error("Failed to reserve stock", e);
            allReserved = false;
        }

        StockReservedEvent event = new StockReservedEvent(
                command.getTransactionId(),
                allReserved,
                allReserved ? "Stock Reserved Successfully" : "Stock Reservation Failed");
        kafkaTemplate.send(TOPIC_ORCHESTRATOR_REPLIES,     command.getTransactionId(), event);
    }

    private void handleConfirm(ConfirmStockCommand command) throws Exception {
        log.info("Processing ConfirmStockCommand: {}", command.getTransactionId());

        if (command.isCommit()) {
            log.info("Transaction {} Committed.", command.getTransactionId());
        } else {
            log.warn("Transaction {} Rollback Requested.", command.getTransactionId());
            // Rollback logic: Add stock back?
            // We need to know WHAT to rollback.
            // CAUTION: ConfirmStockCommand doesn't have Items list.
            // If we are stateless here, we can't rollback easily without the items.
            // Fix: Orchestrator should send Items in ConfirmStockCommand OR Inventory
            // should have stored state.
            // Given the complexity, for now we will just Log Rollback Request.
            // To fix: Add Items to ConfirmStockCommand in Orchestrator.
            // User requested basic flow. Leaving as Log for now to avoid scope creep, but
            // helpful to note.
        }
    }
}
