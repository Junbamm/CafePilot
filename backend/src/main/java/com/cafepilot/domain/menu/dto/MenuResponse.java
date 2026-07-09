package com.cafepilot.domain.menu.dto;

import com.cafepilot.domain.menu.entity.Menu;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MenuResponse(
        Long id,
        Long cafeId,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean isAvailable,
        int displayOrder,
        LocalDateTime createdAt
) {
    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getCafeId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice(),
                menu.getCategory().name(),
                menu.isAvailable(),
                menu.getDisplayOrder(),
                menu.getCreatedAt()
        );
    }
}
