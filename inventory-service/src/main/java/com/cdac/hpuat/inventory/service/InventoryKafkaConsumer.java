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
import com.cdac.hpuat.inventory.dto.command.RollbackStockCommand;
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
            } else if (message instanceof RollbackStockCommand) {
                handleRollback((RollbackStockCommand) message);
            } else {
                log.warn("Unknown message type: {}", message.getClass().getName());
            }
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }

    private void handleReserve(ReserveStockCommand command) {
        log.info("Processing ReserveStockCommand: {}", command.getTransactionId());

        try {
            inventoryService.reserveStock(
                    command.getTransactionId(),
                    command.getGnumHospitalCode(),
                    command.getHstnumStoreId(),
                    command.getItems());

            // Send StockReservedEvent (Success)
            StockReservedEvent event = new StockReservedEvent(
                    command.getTransactionId(),
                    true,
                    "Stock Reserved Successfully");

            kafkaTemplate.send(TOPIC_ORCHESTRATOR_REPLIES, command.getTransactionId(), event);

        } catch (Exception e) {
            log.error("Stock Reservation Failed", e);
            // Send Failure Event
            StockReservedEvent event = new StockReservedEvent(
                    command.getTransactionId(),
                    false,
                    "Stock Reservation Failed: " + e.getMessage());
            kafkaTemplate.send(TOPIC_ORCHESTRATOR_REPLIES, command.getTransactionId(), event);
        }
    }

    private void handleConfirm(ConfirmStockCommand command) {
        log.info("Handling ConfirmStockCommand (Commit) for Transaction: {}", command.getTransactionId());
        // Logic to finalize stock if you had a temporary hold state.
        // Currently we deduct immediately, so this might just be an acknowledgment or
        // no-op.
    }

    private void handleRollback(RollbackStockCommand command) {
        log.info("Handling RollbackStockCommand for Transaction: {}", command.getTransactionId());
        try {
            for (IssueItemDto item : command.getItems()) {
                inventoryService.releaseStock(
                        command.getTransactionId(),
                        command.getGnumHospitalCode(),
                        command.getHstnumStoreId(),
                        item.getHstnumItembrandId(),
                        item.getHststrBatchSlNo(),
                        item.getHstnumIssueQty().intValue());
            }
            log.info("Rollback successful for Transaction: {}", command.getTransactionId());
        } catch (Exception e) {
            log.error("Failed to rollback stock for Transaction: {}", command.getTransactionId(), e);
        }
    }
}
