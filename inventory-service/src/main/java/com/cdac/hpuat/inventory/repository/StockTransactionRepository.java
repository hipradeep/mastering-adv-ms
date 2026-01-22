package com.cdac.hpuat.inventory.repository;

import com.cdac.hpuat.inventory.entity.HsttStockTransactionDtl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransactionRepository extends JpaRepository<HsttStockTransactionDtl, Long> {
}
