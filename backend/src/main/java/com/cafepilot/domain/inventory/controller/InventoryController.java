package com.cafepilot.domain.inventory.controller;

import com.cafepilot.domain.inventory.dto.AdjustInventoryRequest;
import com.cafepilot.domain.inventory.dto.InventoryResponse;
import com.cafepilot.domain.inventory.dto.UpdateThresholdRequest;
import com.cafepilot.domain.inventory.service.InventoryService;
import com.cafepilot.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory", description = "재고 API")
@RestController
@RequestMapping("/api/v1/cafes/{cafeId}/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "전체 재고 조회")
    @GetMapping
    public ApiResponse<List<InventoryResponse>> getInventories(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId
    ) {
        return ApiResponse.success(inventoryService.getInventories(memberId, cafeId));
    }

    @Operation(summary = "부족 재고 목록 조회")
    @GetMapping("/low-stock")
    public ApiResponse<List<InventoryResponse>> getLowStockInventories(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId
    ) {
        return ApiResponse.success(inventoryService.getLowStockInventories(memberId, cafeId));
    }

    @Operation(summary = "재고 수량 조정 (입고/차감)")
    @PatchMapping("/menus/{menuId}/adjust")
    public ApiResponse<InventoryResponse> adjustInventory(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @PathVariable Long menuId,
            @Valid @RequestBody AdjustInventoryRequest request
    ) {
        return ApiResponse.success(inventoryService.adjustInventory(memberId, cafeId, menuId, request));
    }

    @Operation(summary = "부족 재고 임계값 수정")
    @PatchMapping("/menus/{menuId}/threshold")
    public ApiResponse<InventoryResponse> updateThreshold(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @PathVariable Long menuId,
            @Valid @RequestBody UpdateThresholdRequest request
    ) {
        return ApiResponse.success(inventoryService.updateThreshold(memberId, cafeId, menuId, request));
    }
}
