package com.cafepilot.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(

        @NotNull(message = "메뉴 ID는 필수입니다.")
        Long menuId,

        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        int quantity
) {
}
