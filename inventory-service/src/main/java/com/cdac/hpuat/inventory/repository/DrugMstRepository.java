package com.cdac.hpuat.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.hpuat.inventory.entity.HsttDrugMst;
import com.cdac.hpuat.inventory.entity.entityids.HsttDrugMstPK;

@Repository
public interface DrugMstRepository extends JpaRepository<HsttDrugMst, HsttDrugMstPK> {

}
