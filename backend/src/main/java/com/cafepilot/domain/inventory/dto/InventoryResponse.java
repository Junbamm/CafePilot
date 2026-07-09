package com.cafepilot.domain.inventory.dto;

import com.cafepilot.domain.inventory.entity.Inventory;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long menuId,
        Long cafeId,
        int quantity,
        int lowStockThreshold,
        boolean isLowStock,
        LocalDateTime updatedAt
) {
    public static InventoryResponse from(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getMenuId(),
                inventory.getCafeId(),
                inventory.getQuantity(),
                inventory.getLowStockThreshold(),
                inventory.isLowStock(),
                inventory.getUpdatedAt()
        );
    }
}
