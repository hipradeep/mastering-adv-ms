package com.cdac.hpuat.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cdac.hpuat.inventory.entity.HsttDrugbrandMst;
import com.cdac.hpuat.inventory.entity.entityids.HsttDrugbrandMstPK;

@Repository
public interface DrugbrandMstRepository extends JpaRepository<HsttDrugbrandMst, HsttDrugbrandMstPK> {

}
