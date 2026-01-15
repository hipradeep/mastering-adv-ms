package com.cdac.hpuat.inventory.service.impl;

import java.util.List;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cdac.hpuat.inventory.dto.StockResponseDto;
import com.cdac.hpuat.inventory.dto.DrugResponseDto;
import com.cdac.hpuat.inventory.dto.DrugBrandResponseDto;
import com.cdac.hpuat.inventory.entity.HsttDrugCurrstockDtl;
import com.cdac.hpuat.inventory.entity.HsttDrugMst;
import com.cdac.hpuat.inventory.entity.HsttDrugbrandMst;
import com.cdac.hpuat.inventory.entity.entityids.HsttDrugMstPK;
import com.cdac.hpuat.inventory.entity.entityids.HsttDrugbrandMstPK;
import com.cdac.hpuat.inventory.repository.DrugCurrstockDtlRepository;
import com.cdac.hpuat.inventory.repository.DrugMstRepository;
import com.cdac.hpuat.inventory.repository.DrugbrandMstRepository;
import com.cdac.hpuat.inventory.service.InventoryService;
import com.cdac.hpuat.inventory.util.BeanUtil;

import com.cdac.hpuat.inventory.exception.ResourceNotFoundException;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);
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
        log.info("hospitalCode :  " + hospitalCode);
        log.info("storeId :  " + storeId);
        log.info("itemBrandId :  " + itemBrandId);
        log.info("batchNo :  " + batchNo);
        log.info("quantity :  " + quantity);

        int updatedRows = stockRepository.updateStockBatch(hospitalCode, storeId, itemBrandId, batchNo, quantity);
        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Stock update failed. Either record not found or insufficient stock for Batch: " + batchNo);
        }
        log.info("Stock deducted successfully for Batch: " + batchNo);
    }
    @Override
    @Transactional
    public void updateStock(Integer hospitalCode, Integer storeId, Integer itemBrandId, String batchNo,
            Integer quantity) {
        log.info("hospitalCode :  " + hospitalCode);
        log.info("storeId :  " + storeId);
        log.info("itemBrandId :  " + itemBrandId);
        log.info("batchNo :  " + batchNo);
        log.info("quantity :  " + quantity);

        int updatedRows = stockRepository.deductStock(hospitalCode, storeId, itemBrandId, batchNo, quantity);
        if (updatedRows == 0) {
            throw new RuntimeException(
                    "Stock update failed. Either record not found or insufficient stock for Batch: " + batchNo);
        }
        log.info("Stock deducted successfully for Batch: " + batchNo);
    }
}
