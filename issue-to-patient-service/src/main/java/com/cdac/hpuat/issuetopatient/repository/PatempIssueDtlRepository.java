package com.cdac.hpuat.issuetopatient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cdac.hpuat.issuetopatient.entity.HsttPatempIssueDtl;
import com.cdac.hpuat.issuetopatient.entity.entityids.HsttPatempIssueDtlPK;

@Repository
public interface PatempIssueDtlRepository extends JpaRepository<HsttPatempIssueDtl, HsttPatempIssueDtlPK> {

    @Query("SELECT COALESCE(MAX(e.hstnumIssueNo), 0) FROM HsttPatempIssueDtl e WHERE e.hstnumStoreId = :storeId AND e.gnumHospitalCode = :hospitalCode")
    Integer findMaxIssueNo(Integer storeId, Integer hospitalCode);

    @Query(value = "SELECT * FROM hstt_patemp_issue_dtl WHERE hrgnum_puk = :puk AND gnum_hospital_code = :hospitalCode", nativeQuery = true)
    List<HsttPatempIssueDtl> findIssuesByPuk(String puk, Integer hospitalCode);
}
