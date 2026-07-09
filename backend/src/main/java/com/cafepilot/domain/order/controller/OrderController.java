package com.cafepilot.domain.order.controller;

import com.cafepilot.domain.order.dto.CreateOrderRequest;
import com.cafepilot.domain.order.dto.OrderResponse;
import com.cafepilot.domain.order.service.OrderService;
import com.cafepilot.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/v1/cafes/{cafeId}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 목록 조회 (페이징)")
    @GetMapping
    public ApiResponse<Page<OrderResponse>> getOrders(
            @PathVariable Long cafeId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.success(orderService.getOrders(cafeId, pageable));
    }

    @Operation(summary = "주문 상세 조회")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @PathVariable Long cafeId,
            @PathVariable Long orderId
    ) {
        return ApiResponse.success(orderService.getOrder(cafeId, orderId));
    }

    @Operation(summary = "주문 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        return ApiResponse.success(orderService.createOrder(memberId, cafeId, request));
    }

    @Operation(summary = "주문 상태 변경 (accept/prepare/complete/cancel)")
    @PatchMapping("/{orderId}/status")
    public ApiResponse<OrderResponse> changeStatus(
            @PathVariable Long cafeId,
            @PathVariable Long orderId,
            @RequestParam String action
    ) {
        return ApiResponse.success(orderService.changeStatus(cafeId, orderId, action));
    }
}
