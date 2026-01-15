package com.cdac.hpuat.issuetopatient.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cdac.hpuat.issuetopatient.dto.command.CreateIssueCommand;
import com.cdac.hpuat.issuetopatient.dto.event.IssueCreatedEvent;
import com.cdac.hpuat.issuetopatient.dto.IssueRequestDto;
import com.cdac.hpuat.issuetopatient.dto.IssueItemDto;

import static com.cdac.hpuat.issuetopatient.config.KafkaConfigProperties.*;

@Service
public class IssueKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(IssueKafkaConsumer.class);

    @Autowired
    private IssueToPatientService issueService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = TOPIC_ISSUE_COMMANDS, groupId = GROUP_ID_ISSUE)
    public void consume(CreateIssueCommand command) {
        log.info("Received Message in group issue-group: {}", command);
        try {
            log.info("Processing CreateIssueCommand for Transaction: {}", command.getTransactionId());

            try {
                // Map Command to Request DTO
                IssueRequestDto requestDto = new IssueRequestDto();
                requestDto.setGnumHospitalCode(command.getGnumHospitalCode());
                requestDto.setHstnumStoreId(command.getHstnumStoreId());
                requestDto.setHrgnumPuk(command.getHrgnumPuk());
                requestDto.setHststrPatientName(command.getHststrPatientName());
                requestDto.setGstrRemarks(command.getGstrRemarks());

                List<IssueItemDto> items = new ArrayList<>();
                for (IssueItemDto cmdItem : command.getItems()) {
                    IssueItemDto item = new IssueItemDto();
                    item.setHstnumItembrandId(cmdItem.getHstnumItembrandId());
                    item.setHststrBatchSlNo(cmdItem.getHststrBatchSlNo());
                    item.setHstnumIssueQty(cmdItem.getHstnumIssueQty());
                    item.setHstnumStoreId(cmdItem.getHstnumStoreId());
                    items.add(item);
                }
                requestDto.setItems(items);

                // Call Service
                issueService.createIssue(requestDto);

                // Send Success Event
                IssueCreatedEvent event = new IssueCreatedEvent(
                        command.getTransactionId(),
                        true,
                        "Issue Created Successfully");
                kafkaTemplate.send(TOPIC_ORCHESTRATOR_REPLIES,  command.getTransactionId(), event);

            } catch (Exception e) {
                log.error("Failed to create issue", e);
                // Send Failure Event
                IssueCreatedEvent event = new IssueCreatedEvent(
                        command.getTransactionId(),
                        false,
                        "Failed to create issue: " + e.getMessage());
                kafkaTemplate.send(
                        TOPIC_ORCHESTRATOR_REPLIES,
                        command.getTransactionId(), event);
            }

        } catch (Exception e) {
            log.error("Error processing message", e);
        }
    }
}
