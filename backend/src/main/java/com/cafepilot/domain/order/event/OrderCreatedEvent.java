package com.cafepilot.domain.order.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        Long cafeId,
        Long memberId,
        BigDecimal totalAmount
) {
}
