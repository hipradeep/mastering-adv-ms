package com.cdac.hpuat.issuetopatient.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cdac.hpuat.issuetopatient.entity.HsttPatempIssueItemDtl;
import com.cdac.hpuat.issuetopatient.entity.entityids.HsttPatempIssueItemDtlPK;

@Repository
public interface PatempIssueItemDtlRepository
                extends JpaRepository<HsttPatempIssueItemDtl, HsttPatempIssueItemDtlPK> {

        @Query(value = "SELECT * FROM hstt_patemp_issue_item_dtl WHERE hstnum_issue_no = :issueNo AND hstnum_store_id = :storeId AND gnum_hospital_code = :hospitalCode", nativeQuery = true)
        List<HsttPatempIssueItemDtl> findItemsByIssueDetails(Integer issueNo, Integer storeId, Integer hospitalCode);

        @Query(value = "SELECT * FROM hstt_patemp_issue_item_dtl WHERE (:storeId is null OR :storeId = 0 OR hstnum_store_id = :storeId) AND gnum_hospital_code = :hospitalCode ORDER BY gdt_entry_date DESC LIMIT 10", nativeQuery = true)
        List<HsttPatempIssueItemDtl> findLatestIssues(Integer storeId, Integer hospitalCode);
}
