package com.cdac.hpuat.inventory.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cdac.hpuat.inventory.entity.HsttDrugCurrstockDtl;
import com.cdac.hpuat.inventory.entity.entityids.HsttDrugCurrstockDtlPK;

@Repository
public interface DrugCurrstockDtlRepository extends JpaRepository<HsttDrugCurrstockDtl, HsttDrugCurrstockDtlPK> {

        @Query(value = "SELECT * FROM dwh.hstt_drug_currstock_dtl WHERE gnum_hospital_code = :hospitalCode AND (:storeId is null or :storeId = 0 or hstnum_store_id = :storeId) AND (:itemBrandId is null or :itemBrandId = 0 or hstnum_itembrand_id = :itemBrandId) AND hstnum_inhand_qty > 0 AND hstdt_expiry_date > NOW()", nativeQuery = true)
        List<HsttDrugCurrstockDtl> findStockWithOptionalParams(@Param("hospitalCode") Integer hospitalCode,
                        @Param("storeId") Integer storeId, @Param("itemBrandId") Integer itemBrandId);

        @Query(value = "SELECT * FROM dwh.hstt_drug_currstock_dtl WHERE gnum_hospital_code = :hospitalCode AND   hstnum_store_id = :storeId  AND hstnum_itembrand_id = :itemBrandId  AND hststr_batch_no = :batchNo AND hstnum_inhand_qty > 0 AND hstdt_expiry_date > NOW()", nativeQuery = true)
        List<HsttDrugCurrstockDtl> findStockWithItemBatch(@Param("hospitalCode") Integer hospitalCode,
                        @Param("storeId") Integer storeId, @Param("itemBrandId") Integer itemBrandId,
                        @Param("batchNo") String batchNo);

        @Modifying
        @Query(value = "UPDATE dwh.hstt_drug_currstock_dtl SET hstnum_inhand_qty = :quantity WHERE hstnum_store_id = :storeId AND hstnum_itembrand_id = :itemBrandId AND gnum_hospital_code = :hospitalCode", nativeQuery = true)
        int updateStock(@Param("hospitalCode") Integer hospitalCode, @Param("storeId") Integer storeId,
                        @Param("itemBrandId") Integer itemBrandId, @Param("quantity") Integer quantity);

        @Modifying
        @Query(value = "UPDATE dwh.hstt_drug_currstock_dtl SET hstnum_inhand_qty = :newQuantity WHERE hstnum_store_id = :storeId AND hstnum_itembrand_id = :itemBrandId AND gnum_hospital_code = :hospitalCode AND hststr_batch_no = :batchNo  ", nativeQuery = true)
        int updateStockBatch(@Param("hospitalCode") Integer hospitalCode, @Param("storeId") Integer storeId,
                        @Param("itemBrandId") Integer itemBrandId, @Param("batchNo") String batchNo,
                        @Param("newQuantity") Integer newQuantity);

        @Modifying
        @Query(value = "UPDATE dwh.hstt_drug_currstock_dtl SET hstnum_inhand_qty = hstnum_inhand_qty - :deductQty WHERE hstnum_store_id = :storeId AND hstnum_itembrand_id = :itemBrandId AND hststr_batch_no = :batchNo AND hstnum_inhand_qty >= :deductQty AND gnum_hospital_code = :hospitalCode", nativeQuery = true)
        int deductStock(@Param("hospitalCode") Integer hospitalCode, @Param("storeId") Integer storeId,
                        @Param("itemBrandId") Integer itemBrandId, @Param("batchNo") String batchNo,
                        @Param("deductQty") Integer deductQty);
}
