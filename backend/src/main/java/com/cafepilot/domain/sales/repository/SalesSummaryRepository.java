package com.cafepilot.domain.sales.repository;

import com.cafepilot.domain.sales.entity.SalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SalesSummaryRepository extends JpaRepository<SalesSummary, Long> {

    List<SalesSummary> findByCafeIdAndSummaryDateBetweenOrderBySummaryDateDesc(
            Long cafeId, LocalDate from, LocalDate to);

    Optional<SalesSummary> findByCafeIdAndSummaryDate(Long cafeId, LocalDate summaryDate);
}
