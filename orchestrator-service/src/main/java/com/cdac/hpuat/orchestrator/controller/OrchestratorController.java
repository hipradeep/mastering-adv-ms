package com.cdac.hpuat.orchestrator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cdac.hpuat.orchestrator.dto.ApiResponse;
import com.cdac.hpuat.orchestrator.dto.IssueRequestDto;
import com.cdac.hpuat.orchestrator.service.OrchestratorService;
import com.cdac.hpuat.orchestrator.entity.HsttSagaTransaction;
import com.cdac.hpuat.orchestrator.service.SagaTransactionService;

@RestController
@RequestMapping("/api/orchestrator")
public class OrchestratorController {

    @Autowired
    private OrchestratorService orchestratorService;

    @Autowired
    private SagaTransactionService sagaTransactionService;

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<String>> initiateIssueProcess(@RequestBody IssueRequestDto requestDto) {
        // Ensure hospital code is set if missing (default 998 like other services)
        if (requestDto.getGnumHospitalCode() == null) {
            requestDto.setGnumHospitalCode(998);
        }

        String transactionId = orchestratorService.initiateIssueTransaction(requestDto);
        return ResponseEntity
                .ok(ApiResponse.success("Issue Process Initiated. Transaction ID: " + transactionId, transactionId));
    }

    @GetMapping("/status/{transactionId}")
    public ResponseEntity<ApiResponse<String>> getTransactionStatus(@PathVariable String transactionId) {
        HsttSagaTransaction transaction = sagaTransactionService.getTransactionStatus(transactionId);
        return ResponseEntity
                .ok(ApiResponse.success("Transaction Status Fetched", transaction.getHststrStatus().name()));
    }
}
