package com.cafepilot.domain.sales.dto;

import com.cafepilot.domain.sales.entity.SalesSummary;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesSummaryResponse(
        Long id,
        Long cafeId,
        LocalDate summaryDate,
        BigDecimal totalRevenue,
        int orderCount,
        Long bestSellingMenuId
) {
    public static SalesSummaryResponse from(SalesSummary summary) {
        return new SalesSummaryResponse(
                summary.getId(),
                summary.getCafeId(),
                summary.getSummaryDate(),
                summary.getTotalRevenue(),
                summary.getOrderCount(),
                summary.getBestSellingMenuId()
        );
    }
}
