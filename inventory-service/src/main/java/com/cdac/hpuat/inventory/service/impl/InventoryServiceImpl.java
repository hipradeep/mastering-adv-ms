package com.cdac.hpuat.inventory.service.impl;

import java.util.List;

import com.cdac.hpuat.inventory.dto.DrugBrandResponseDto;
import com.cdac.hpuat.inventory.dto.DrugResponseDto;
import com.cdac.hpuat.inventory.dto.IssueItemDto;
import com.cdac.hpuat.inventory.dto.StockResponseDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cdac.hpuat.inventory.entity.HsttDrugCurrstockDtl;
import com.cdac.hpuat.inventory.entity.HsttDrugMst;
import com.cdac.hpuat.inventory.entity.HsttDrugbrandMst;
import com.cdac.hpuat.inventory.entity.HsttStockTransactionDtl;
import com.cdac.hpuat.inventory.entity.entityids.HsttDrugMstPK;
import com.cdac.hpuat.inventory.entity.entityids.HsttDrugbrandMstPK;
import com.cdac.hpuat.inventory.repository.DrugCurrstockDtlRepository;
import com.cdac.hpuat.inventory.repository.DrugMstRepository;
import com.cdac.hpuat.inventory.repository.DrugbrandMstRepository;
import com.cdac.hpuat.inventory.repository.StockTransactionRepository;
import com.cdac.hpuat.inventory.service.InventoryService;
import com.cdac.hpuat.inventory.util.BeanUtil;

import com.cdac.hpuat.inventory.exception.ResourceNotFoundException;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
    @Autowired
    private StockTransactionRepository transactionRepository;

    @Autowired
    private DrugCurrstockDtlRepository stockRepository;

    @Autowired
    private DrugMstRepository drugRepository;

    @Autowired
    private DrugbrandMstRepository brandRepository;

    @Override
    public List<StockResponseDto> getStockItem(Integer hospitalCode, Integer storeId, Integer itemBrandId) {
        List<HsttDrugCurrstockDtl> stockList = stockRepository.findStockWithOptionalParams(hospitalCode, storeId,
                itemBrandId);
        return BeanUtil.copyListProperties(stockList, StockResponseDto.class);
    }

    @Override
    public DrugResponseDto getDrugDetails(Integer hospitalCode, Integer itemId) {
        HsttDrugMstPK pk = new HsttDrugMstPK(itemId, hospitalCode);
        HsttDrugMst drug = drugRepository.findById(pk)
                .orElseThrow(() -> new ResourceNotFoundException("Drug not found with ID: " + itemId));
        return BeanUtil.copyProperties(drug, DrugResponseDto.class);
    }

    @Override
    public DrugBrandResponseDto getBrandDetails(Integer hospitalCode, Integer brandId) {
        HsttDrugbrandMstPK pk = new HsttDrugbrandMstPK(brandId, hospitalCode);
        HsttDrugbrandMst brand = brandRepository.findById(pk)
                .orElseThrow(() -> new ResourceNotFoundException("Drug Brand not found with ID: " + brandId));
        return BeanUtil.copyProperties(brand, DrugBrandResponseDto.class);
    }

    @Override
    @Transactional
    public void updateStockBatch(Integer hospitalCode, Integer storeId, Integer itemBrandId, String batchNo,
            Integer quantity) {
        // Legacy or direct update without transaction log - keeping as is or should I
        // deprecate?
        // Leaving as is for now as per instructions to only audit Saga flows if
        // possible.
        int updatedRows = stockRepository.updateStockBatch(hospitalCode, storeId, itemBrandId, batchNo, quantity);
        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Stock update failed. Either record not found or insufficient stock for Batch: " + batchNo);
        }
    }

    @Override
    @Transactional
    public void updateStock(Integer hospitalCode, Integer storeId, Integer itemBrandId, String batchNo,
            Integer quantity) {
        // This was the old single-item update. Keeping for compatibility but mostly
        // unused by Saga now.
        int updatedRows = stockRepository.deductStock(hospitalCode, storeId, itemBrandId, batchNo, quantity);
        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Stock update failed. Either record not found or insufficient stock for Batch: " + batchNo);
        }
    }

    private void logTransaction(String transactionId, Integer hospitalCode, Integer storeId, Integer itemBrandId,
            String batchNo, Double quantity, String type) {
        HsttDrugCurrstockDtl stock = stockRepository.findStockWithOptionalParams(hospitalCode, storeId, itemBrandId)
                .stream().filter(s -> s.getHststrBatchNo().equals(batchNo)).findFirst()
                .orElse(null);

        if (stock == null) {
            log.warn("Stock Log: Record not found for logging " + batchNo);
            return;
        }

        HsttStockTransactionDtl trans = new HsttStockTransactionDtl();
        trans.setHststrSagaTransId(transactionId);
        trans.setGnumHospitalCode(hospitalCode);
        trans.setHstnumStoreId(storeId);
        trans.setHstnumItembrandId(itemBrandId);
        trans.setHststrBatchNo(batchNo);
        trans.setHstnumTransferQty(quantity);
        trans.setHststrTransType(type);
        trans.setHstnumInhandQtyBefore(stock.getHstnumInhandQty());

        if ("RESERVE".equals(type)) {
            trans.setHstnumInhandQtyAfter(stock.getHstnumInhandQty() - quantity);
        } else {
            trans.setHstnumInhandQtyAfter(stock.getHstnumInhandQty() + quantity);
        }

        transactionRepository.save(trans);
    }

    @Override
    @Transactional
    public void releaseStock(String transactionId, Integer hospitalCode, Integer storeId, Integer itemBrandId,
            String batchNo,
            Integer quantity) {
        log.info("Releasing Stock - TransID: {} Hosp: {}", transactionId, hospitalCode);

        // Log Before Action (Need to fetch logic inside logTransaction)
        try {
            logTransaction(transactionId, hospitalCode, storeId, itemBrandId, batchNo, Double.valueOf(quantity),
                    "RELEASE");
        } catch (Exception e) {
            log.error("Failed to log stock transaction", e);
        }

        int updatedRows = stockRepository.releaseReservedStock(hospitalCode, storeId, itemBrandId, batchNo, quantity);
        if (updatedRows == 0) {
            log.warn("Stock release failed. Record not found for Batch " + batchNo);
        } else {
            log.info("Stock released successfully for Batch: " + batchNo);
        }
    }

    @Override
    @Transactional
    public void reserveStock(String transactionId, Integer hospitalCode, Integer storeId, List<IssueItemDto> items) {
        log.info("Starting Batch Reservation - TransID: {} for Store: {}", transactionId, storeId);

        for (IssueItemDto item : items) {

//            if(item.getHstnumIssueQty()%2==0){
//                int i = 1 / 0;
//            }

            // Log Transaction
            try {
                logTransaction(transactionId, hospitalCode, storeId, item.getHstnumItembrandId(),
                        item.getHststrBatchSlNo(), item.getHstnumIssueQty(), "RESERVE");
            } catch (Exception e) {
                log.error("Failed to log stock transaction", e);
            }

            // Deduct Stock
            int updatedRows = stockRepository.deductStock(hospitalCode, storeId, item.getHstnumItembrandId(),
                    item.getHststrBatchSlNo(), item.getHstnumIssueQty().intValue());
            if (updatedRows == 0) {
                throw new RuntimeException(
                        "Stock reservation failed. Insufficient stock for Batch: " + item.getHststrBatchSlNo());
            }
        }
        log.info("Batch Reservation Completed Successfully");
    }
}
