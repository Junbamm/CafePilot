package com.cafepilot.domain.cafe.dto;

import com.cafepilot.domain.cafe.entity.Cafe;

import java.time.LocalDateTime;

public record CafeResponse(
        Long id,
        Long ownerId,
        String name,
        String address,
        String phone,
        boolean isOpen,
        LocalDateTime createdAt
) {
    public static CafeResponse from(Cafe cafe) {
        return new CafeResponse(
                cafe.getId(),
                cafe.getOwnerId(),
                cafe.getName(),
                cafe.getAddress(),
                cafe.getPhone(),
                cafe.isOpen(),
                cafe.getCreatedAt()
        );
    }
}
