package com.cdac.hpuat.inventory.service;

import java.util.List;
import com.cdac.hpuat.inventory.dto.StockResponseDto;
import com.cdac.hpuat.inventory.dto.DrugResponseDto;
import com.cdac.hpuat.inventory.dto.DrugBrandResponseDto;
import com.cdac.hpuat.inventory.dto.IssueItemDto;

public interface InventoryService {

    List<StockResponseDto> getStockItem(Integer hospitalCode, Integer storeId, Integer itemBrandId);

    DrugResponseDto getDrugDetails(Integer hospitalCode, Integer itemId);

    DrugBrandResponseDto getBrandDetails(Integer hospitalCode, Integer brandId);

    void updateStock(Integer hospitalCode, Integer storeId, Integer itemBrandId, String batchNo, Integer quantity);

    void releaseStock(String transactionId, Integer hospitalCode, Integer storeId, Integer itemBrandId, String batchNo,
            Integer quantity);

    void reserveStock(String transactionId, Integer hospitalCode, Integer storeId, List<IssueItemDto> items);

    void updateStockBatch(Integer hospitalCode, Integer storeId, Integer itemBrandId, String batchNo, Integer quantity);
}
