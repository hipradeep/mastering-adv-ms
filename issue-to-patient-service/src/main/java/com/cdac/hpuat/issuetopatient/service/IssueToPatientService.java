package com.cdac.hpuat.issuetopatient.service;

import java.util.List;
import com.cdac.hpuat.issuetopatient.dto.IssueRequestDto;
import com.cdac.hpuat.issuetopatient.dto.IssueResponseDto;
import com.cdac.hpuat.issuetopatient.dto.IssueItemResponseDto;

public interface IssueToPatientService {
    String createIssue(IssueRequestDto issueRequestDto);

    IssueResponseDto getIssueDetails(Integer hospitalCode, Integer storeId, Integer issueNo);

    List<IssueResponseDto> getPatientIssueHistory(Integer hospitalCode, String crNo);

    List<IssueItemResponseDto> getLatestIssues(Integer hospitalCode, Integer storeId);
}
