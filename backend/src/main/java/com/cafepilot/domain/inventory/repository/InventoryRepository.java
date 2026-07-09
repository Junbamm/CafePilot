package com.cafepilot.domain.inventory.repository;

import com.cafepilot.domain.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByMenuId(Long menuId);

    List<Inventory> findByCafeId(Long cafeId);

    List<Inventory> findByCafeIdAndQuantityLessThanEqual(Long cafeId, int threshold);
}
