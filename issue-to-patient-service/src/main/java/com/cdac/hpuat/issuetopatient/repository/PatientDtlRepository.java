package com.cdac.hpuat.issuetopatient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.hpuat.issuetopatient.entity.HsttPatientDtl;
import com.cdac.hpuat.issuetopatient.entity.entityids.HsttPatientDtlPK;

@Repository
public interface PatientDtlRepository extends JpaRepository<HsttPatientDtl, HsttPatientDtlPK> {

}
