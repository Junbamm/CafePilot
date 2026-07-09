package com.cafepilot.domain.inventory.service;

import com.cafepilot.domain.cafe.entity.Cafe;
import com.cafepilot.domain.cafe.exception.CafeException;
import com.cafepilot.domain.cafe.repository.CafeRepository;
import com.cafepilot.domain.inventory.dto.AdjustInventoryRequest;
import com.cafepilot.domain.inventory.dto.InventoryResponse;
import com.cafepilot.domain.inventory.dto.UpdateThresholdRequest;
import com.cafepilot.domain.inventory.entity.Inventory;
import com.cafepilot.domain.inventory.exception.InventoryException;
import com.cafepilot.domain.inventory.repository.InventoryRepository;
import com.cafepilot.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final CafeRepository cafeRepository;

    public List<InventoryResponse> getInventories(Long memberId, Long cafeId) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        return inventoryRepository.findByCafeId(cafeId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }

    public List<InventoryResponse> getLowStockInventories(Long memberId, Long cafeId) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        return inventoryRepository.findByCafeId(cafeId).stream()
                .filter(Inventory::isLowStock)
                .map(InventoryResponse::from)
                .toList();
    }

    @Transactional
    public InventoryResponse adjustInventory(Long memberId, Long cafeId, Long menuId,
                                             AdjustInventoryRequest request) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        Inventory inventory = inventoryRepository.findByMenuId(menuId)
                .orElseThrow(() -> new InventoryException(ErrorCode.INV_NOT_FOUND));

        if (request.type() == AdjustInventoryRequest.AdjustType.INCREASE) {
            inventory.increase(request.amount());
        } else {
            inventory.decrease(request.amount());
        }

        return InventoryResponse.from(inventory);
    }

    @Transactional
    public InventoryResponse updateThreshold(Long memberId, Long cafeId, Long menuId,
                                             UpdateThresholdRequest request) {
        Cafe cafe = findActiveCafe(cafeId);
        validateOwner(cafe, memberId);

        Inventory inventory = inventoryRepository.findByMenuId(menuId)
                .orElseThrow(() -> new InventoryException(ErrorCode.INV_NOT_FOUND));

        inventory.updateThreshold(request.lowStockThreshold());
        return InventoryResponse.from(inventory);
    }

    private Cafe findActiveCafe(Long cafeId) {
        return cafeRepository.findByIdAndDeletedAtIsNull(cafeId)
                .orElseThrow(() -> new CafeException(ErrorCode.CAFE_NOT_FOUND));
    }

    private void validateOwner(Cafe cafe, Long memberId) {
        if (!cafe.isOwnedBy(memberId)) {
            throw new CafeException(ErrorCode.CAFE_ACCESS_DENIED);
        }
    }
}
