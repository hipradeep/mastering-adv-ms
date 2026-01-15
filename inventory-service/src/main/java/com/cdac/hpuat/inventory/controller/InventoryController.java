package com.cdac.hpuat.inventory.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cdac.hpuat.inventory.dto.ApiResponse;
import com.cdac.hpuat.inventory.dto.DrugBrandResponseDto;
import com.cdac.hpuat.inventory.dto.DrugResponseDto;
import com.cdac.hpuat.inventory.dto.StockResponseDto;
import com.cdac.hpuat.inventory.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")

public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<List<StockResponseDto>>> getStock(
            @RequestParam(defaultValue = "998") Integer hospitalCode,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) Integer itemBrandId) {

        List<StockResponseDto> response = inventoryService.getStockItem(hospitalCode, storeId, itemBrandId);

        return new ResponseEntity<>(ApiResponse.success("Stock fetched successfully", response), HttpStatus.OK);
    }

    @GetMapping("/drug/{itemId}")
    public ResponseEntity<ApiResponse<DrugResponseDto>> getDrugDetails(
            @PathVariable Integer itemId,
            @RequestParam Integer hospitalCode) {
        DrugResponseDto response = inventoryService.getDrugDetails(hospitalCode, itemId);
        return new ResponseEntity<>(ApiResponse.success("Drug details fetched successfully", response), HttpStatus.OK);
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<ApiResponse<DrugBrandResponseDto>> getBrandDetails(
            @PathVariable Integer brandId,
            @RequestParam Integer hospitalCode) {
        DrugBrandResponseDto response = inventoryService.getBrandDetails(hospitalCode, brandId);
        return new ResponseEntity<>(ApiResponse.success("Brand details fetched successfully", response), HttpStatus.OK);
    }

    @PostMapping("/stock/update")
    public ResponseEntity<ApiResponse<String>> updateStock(@RequestParam(defaultValue = "998") Integer hospitalCode,
            @RequestParam Integer storeId, @RequestParam Integer itemBrandId, @RequestParam String batchNo,
            @RequestParam Integer quantity) {
        inventoryService.updateStockBatch(hospitalCode, storeId, itemBrandId, batchNo, quantity);
        return new ResponseEntity<>(ApiResponse.success("Stock updated successfully", null), HttpStatus.OK);
    }
}
