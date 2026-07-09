package com.cafepilot.domain.order.dto;

import com.cafepilot.domain.order.entity.Order;
import com.cafepilot.domain.order.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long cafeId,
        Long memberId,
        String status,
        BigDecimal totalAmount,
        String note,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
    public record OrderItemResponse(
            Long menuId,
            String menuName,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getMenuId(),
                    item.getMenuName(),
                    item.getUnitPrice(),
                    item.getQuantity(),
                    item.getSubtotal()
            );
        }
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCafeId(),
                order.getMemberId(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getNote(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }
}
