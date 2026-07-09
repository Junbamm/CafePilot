package com.cafepilot.domain.inventory.dto;

import jakarta.validation.constraints.Min;

public record UpdateThresholdRequest(

        @Min(value = 0, message = "임계값은 0 이상이어야 합니다.")
        int lowStockThreshold
) {
}
