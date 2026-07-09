package com.cafepilot.domain.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "주문 항목은 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemRequest> items,

        @Size(max = 500, message = "요청 사항은 500자 이하여야 합니다.")
        String note
) {
}
