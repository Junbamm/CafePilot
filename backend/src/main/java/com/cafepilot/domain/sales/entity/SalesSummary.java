package com.cafepilot.domain.sales.entity;

import com.cafepilot.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
    name = "sales_summaries",
    uniqueConstraints = @UniqueConstraint(columnNames = {"cafe_id", "summary_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SalesSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cafe_id", nullable = false)
    private Long cafeId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRevenue;

    @Column(nullable = false)
    private int orderCount;

    @Column
    private Long bestSellingMenuId;

    @Builder
    private SalesSummary(Long cafeId, LocalDate summaryDate, BigDecimal totalRevenue,
                         int orderCount, Long bestSellingMenuId) {
        this.cafeId = cafeId;
        this.summaryDate = summaryDate;
        this.totalRevenue = totalRevenue;
        this.orderCount = orderCount;
        this.bestSellingMenuId = bestSellingMenuId;
    }

    public static SalesSummary create(Long cafeId, LocalDate summaryDate,
                                      BigDecimal totalRevenue, int orderCount,
                                      Long bestSellingMenuId) {
        return SalesSummary.builder()
                .cafeId(cafeId)
                .summaryDate(summaryDate)
                .totalRevenue(totalRevenue)
                .orderCount(orderCount)
                .bestSellingMenuId(bestSellingMenuId)
                .build();
    }
}
