package com.cafepilot.domain.inventory.entity;

import com.cafepilot.domain.inventory.exception.InventoryException;
import com.cafepilot.global.entity.BaseEntity;
import com.cafepilot.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long menuId;

    @Column(nullable = false)
    private Long cafeId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int lowStockThreshold;

    @Version
    private Long version;

    @Builder
    private Inventory(Long menuId, Long cafeId, int quantity, int lowStockThreshold) {
        this.menuId = menuId;
        this.cafeId = cafeId;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public static Inventory create(Long menuId, Long cafeId, int quantity, int lowStockThreshold) {
        return Inventory.builder()
                .menuId(menuId)
                .cafeId(cafeId)
                .quantity(quantity)
                .lowStockThreshold(lowStockThreshold)
                .build();
    }

    public void increase(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가량은 0보다 커야 합니다.");
        }
        this.quantity += amount;
    }

    public void decrease(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감량은 0보다 커야 합니다.");
        }
        if (this.quantity < amount) {
            throw new InventoryException(ErrorCode.INV_INSUFFICIENT);
        }
        this.quantity -= amount;
    }

    public void updateThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public boolean isLowStock() {
        return this.quantity <= this.lowStockThreshold;
    }
}
