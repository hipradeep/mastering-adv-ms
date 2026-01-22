package com.cdac.hpuat.issuetopatient.service.impl;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cdac.hpuat.issuetopatient.dto.IssueItemDto;
import com.cdac.hpuat.issuetopatient.dto.IssueRequestDto;
import com.cdac.hpuat.issuetopatient.dto.IssueResponseDto;
import com.cdac.hpuat.issuetopatient.dto.IssueItemResponseDto;
import com.cdac.hpuat.issuetopatient.entity.HsttPatempIssueDtl;
import com.cdac.hpuat.issuetopatient.entity.HsttPatempIssueItemDtl;
import com.cdac.hpuat.issuetopatient.entity.entityids.HsttPatempIssueDtlPK;
import com.cdac.hpuat.issuetopatient.repository.PatempIssueDtlRepository;
import com.cdac.hpuat.issuetopatient.repository.PatempIssueItemDtlRepository;
import com.cdac.hpuat.issuetopatient.service.IssueToPatientService;
import com.cdac.hpuat.issuetopatient.util.BeanUtil;
import com.cdac.hpuat.issuetopatient.exception.ResourceNotFoundException;

@Service
public class IssueToPatientServiceImpl implements IssueToPatientService {

    private static final Logger log = LoggerFactory.getLogger(IssueToPatientServiceImpl.class);
    @Autowired
    private PatempIssueDtlRepository issueDtlRepository;

    @Autowired
    private PatempIssueItemDtlRepository issueItemDtlRepository;

    @Override
    public IssueResponseDto getIssueDetails(Integer hospitalCode, Integer storeId, Integer issueNo) {

        HsttPatempIssueDtlPK pk = new HsttPatempIssueDtlPK(storeId, issueNo, hospitalCode);
        HsttPatempIssueDtl issueDtl = issueDtlRepository.findById(pk)
                .orElseThrow(() -> new ResourceNotFoundException("Issue not found with Issue No: " + issueNo));

        List<HsttPatempIssueItemDtl> items = issueItemDtlRepository.findItemsByIssueDetails(issueNo, storeId,
                hospitalCode);

        return mapToResponseDto(issueDtl, items);
    }

    @Override
    @Transactional
    public String createIssue(IssueRequestDto requestDto) {

        Integer storeId = requestDto.getHstnumStoreId();
        Integer hospitalCode = requestDto.getGnumHospitalCode();
        if (hospitalCode == null) {
            hospitalCode = 998;
            requestDto.setGnumHospitalCode(998); // Ensure DTO also has it for copy
        }

        // 1. Generate Issue No
        Integer maxIssueNo = issueDtlRepository.findMaxIssueNo(storeId, hospitalCode);
        Integer nextIssueNo = (maxIssueNo == null ? 0 : maxIssueNo) + 1;

        // 2. Save Parent Issue Dtl
        HsttPatempIssueDtl issueDtl = BeanUtil.copyProperties(requestDto, HsttPatempIssueDtl.class);
        issueDtl.setHstnumIssueNo(nextIssueNo);
        issueDtl.setHstdtIssueDate(new Date());
        issueDtl.setGdtEntryDate(new Date());
        issueDtl.setGnumIsvalid(1);
        issueDtl.setGstrHospitalName("HPUAT Hospital");
        issueDtl.setHstnumServiceTax(6.0); // Default service tax 6%
        issueDtl.setHstnumReturnFlag(0); // Default return flag 0

        // Setting defaults to avoid not-null constraint violations
        issueDtl.setSstnumReqtypeId(1);
        issueDtl.setSstnumItemCatNo(10); // Default Category
        issueDtl.setGnumDeptCode(101); // Default Dept
        issueDtl.setHstdtFinancialStartDate(new Date());
        issueDtl.setHstdtFinancialEndDate(new Date());
        issueDtl.setGnumSeatid(1);
        issueDtl.setHstnumNetCost(0.0);
        issueDtl.setHstnumReturnCost(0.0);
        issueDtl.setGnumGenderCode(1);
        issueDtl.setHstnumPatientType(1);
        issueDtl.setHstnumOutOfStockFlag(0);
        issueDtl.setHstnumDesktopIssueNo(0);
        issueDtl.setGnumDiagnosisCode(0);

        issueDtlRepository.save(issueDtl);

        // 3. Save Item Dtls
        if (requestDto.getItems() != null && !requestDto.getItems().isEmpty()) {
            List<HsttPatempIssueItemDtl> itemList = new ArrayList<>();

            for (IssueItemDto itemDto : requestDto.getItems()) {
                HsttPatempIssueItemDtl itemDtl = BeanUtil.copyProperties(itemDto, HsttPatempIssueItemDtl.class);

                // PK Fields
                itemDtl.setHstnumStoreId(storeId);
                itemDtl.setHstnumIssueNo(nextIssueNo);
                itemDtl.setGnumHospitalCode(hospitalCode);

                // Defaults for fields not in DTO or explicit overrides

                itemDtl.setHstnumMfgId(0);
                itemDtl.setHstnumProgrammeId(0);
                itemDtl.setHstnumStockStatusCode(1);
                itemDtl.setHststrItemSlNo("0");

                itemDtl.setHstdtIssueDate(new Date());
                itemDtl.setGstrRemarks(requestDto.getGstrRemarks()); // Remarks from parent if needed
                itemDtl.setGnumIsvalid(1);
                itemDtl.setGdtEntryDate(new Date());

                // Comprehensive defaults for Item Details

                itemDtl.setHstnumItemId(0);
                itemDtl.setHstnumGroupId(0);
                itemDtl.setHstnumSubgroupId(0);
                itemDtl.setHstnumRate(0.0);
                itemDtl.setHstnumSaleRate(0.0);
                itemDtl.setHstnumRateUnitid(6301);
                itemDtl.setHstnumIssueqtyUnitid(6301);
                itemDtl.setHstnumInhandQty(0.0);
                itemDtl.setHstnumInhandQtyUnitid(6301);
                itemDtl.setHstnumConsumableFlag(1);
                itemDtl.setHstnumReturnQty(0.0);
                itemDtl.setHstnumTotalCost(0.0);
                itemDtl.setHstnumTotalReturnCost(0.0);

                itemList.add(itemDtl);
            }
            issueItemDtlRepository.saveAll(itemList);
        }

        return "Issue Generated Successfully. Issue No: " + nextIssueNo;
    }

    @Override
    public List<IssueResponseDto> getPatientIssueHistory(Integer hospitalCode, String crNo) {
        List<HsttPatempIssueDtl> issues = issueDtlRepository.findIssuesByPuk(crNo, hospitalCode);

        return issues.stream().map(issue -> {
            // Fetch items for each issue
            List<HsttPatempIssueItemDtl> items = issueItemDtlRepository.findItemsByIssueDetails(
                    issue.getHstnumIssueNo(), issue.getHstnumStoreId(), hospitalCode);
            return mapToResponseDto(issue, items);
        }).collect(Collectors.toList());
    }

    @Override
    public List<IssueItemResponseDto> getLatestIssues(Integer hospitalCode, Integer storeId) {
        log.info("hospitalCode  " + hospitalCode);
        log.info("storeId  " + storeId);
        List<HsttPatempIssueItemDtl> items = issueItemDtlRepository.findLatestIssues(storeId, hospitalCode);
        return BeanUtil.copyListProperties(items, IssueItemResponseDto.class);
    }

    private IssueResponseDto mapToResponseDto(HsttPatempIssueDtl issue, List<HsttPatempIssueItemDtl> items) {
        IssueResponseDto dto = BeanUtil.copyProperties(issue, IssueResponseDto.class);

        if (items != null) {
            List<IssueItemResponseDto> itemDtos = BeanUtil.copyListProperties(items, IssueItemResponseDto.class);
            dto.setItems(itemDtos);
        }

        return dto;
    }
}
