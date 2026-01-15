package com.cdac.hpuat.issuetopatient.controller;

import java.util.List;

import com.cdac.hpuat.issuetopatient.dto.IssueItemResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.hpuat.issuetopatient.dto.ApiResponse;
import com.cdac.hpuat.issuetopatient.dto.IssueRequestDto;
import com.cdac.hpuat.issuetopatient.dto.IssueResponseDto;
import com.cdac.hpuat.issuetopatient.service.IssueToPatientService;

@RestController
@RequestMapping("/api/issue")

public class IssueToPatientController {

    @Autowired
    private IssueToPatientService issueService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<String>> createIssue(@RequestBody IssueRequestDto issueRequestDto) {
        String response = issueService.createIssue(issueRequestDto);
        return new ResponseEntity<>(ApiResponse.success("Issue created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<IssueResponseDto>> getIssueDetails(
            @RequestParam(defaultValue = "998") Integer hospitalCode,
            @RequestParam Integer storeId,
            @RequestParam Integer issueNo) {
        IssueResponseDto response = issueService.getIssueDetails(hospitalCode, storeId, issueNo);
        return new ResponseEntity<>(ApiResponse.success("Issue details fetched successfully", response), HttpStatus.OK);
    }

    @GetMapping("/patient")
    public ResponseEntity<ApiResponse<List<IssueResponseDto>>> getPatientIssueHistory(
            @RequestParam(defaultValue = "998") Integer hospitalCode,
            @RequestParam String crNo) {
        List<IssueResponseDto> response = issueService.getPatientIssueHistory(hospitalCode, crNo);
        return new ResponseEntity<>(ApiResponse.success("Patient issue history fetched successfully", response),
                HttpStatus.OK);
    }

    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<IssueItemResponseDto>>> getLatestIssues(
            @RequestParam(defaultValue = "998") Integer hospitalCode,
            @RequestParam(required = false) Integer storeId) {
        List<IssueItemResponseDto> response = issueService.getLatestIssues(hospitalCode, storeId);
        return new ResponseEntity<>(ApiResponse.success("Latest issues fetched successfully", response), HttpStatus.OK);
    }
}
