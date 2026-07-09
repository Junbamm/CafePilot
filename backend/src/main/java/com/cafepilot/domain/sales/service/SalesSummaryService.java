package com.cafepilot.domain.sales.service;

import com.cafepilot.domain.cafe.repository.CafeRepository;
import com.cafepilot.domain.order.entity.Order;
import com.cafepilot.domain.order.entity.OrderItem;
import com.cafepilot.domain.order.repository.OrderRepository;
import com.cafepilot.domain.sales.dto.SalesSummaryResponse;
import com.cafepilot.domain.sales.entity.SalesSummary;
import com.cafepilot.domain.sales.repository.SalesSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesSummaryService {

    private final OrderRepository orderRepository;
    private final SalesSummaryRepository salesSummaryRepository;
    private final CafeRepository cafeRepository;

    @Transactional(readOnly = true)
    public List<SalesSummaryResponse> getSummaries(Long cafeId, LocalDate from, LocalDate to) {
        return salesSummaryRepository
                .findByCafeIdAndSummaryDateBetweenOrderBySummaryDateDesc(cafeId, from, to)
                .stream()
                .map(SalesSummaryResponse::from)
                .toList();
    }

    /**
     * 매일 새벽 1시에 전날 판매 집계 실행
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void aggregateDailySales() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("[SalesSummaryService] 일 판매 집계 시작 date={}", yesterday);

        cafeRepository.findAll().forEach(cafe -> {
            try {
                aggregateSalesForCafe(cafe.getId(), yesterday);
            } catch (Exception e) {
                log.error("[SalesSummaryService] 카페 집계 실패 cafeId={}", cafe.getId(), e);
            }
        });

        log.info("[SalesSummaryService] 일 판매 집계 완료 date={}", yesterday);
    }

    @Transactional
    public void aggregateSalesForCafe(Long cafeId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<Order> completedOrders = orderRepository
                .findByCafeIdOrderByCreatedAtDesc(cafeId, PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .filter(o -> o.getStatus() == Order.Status.COMPLETED)
                .filter(o -> {
                    LocalDateTime createdAt = o.getCreatedAt();
                    return createdAt != null
                            && !createdAt.isBefore(startOfDay)
                            && createdAt.isBefore(endOfDay);
                })
                .toList();

        if (completedOrders.isEmpty()) {
            return;
        }

        BigDecimal totalRevenue = completedOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long bestSellingMenuId = findBestSellingMenuId(completedOrders);

        salesSummaryRepository.findByCafeIdAndSummaryDate(cafeId, date)
                .ifPresentOrElse(
                        existing -> log.info("[SalesSummaryService] 이미 집계된 날짜 cafeId={} date={}", cafeId, date),
                        () -> {
                            SalesSummary summary = SalesSummary.create(
                                    cafeId, date, totalRevenue,
                                    completedOrders.size(), bestSellingMenuId);
                            salesSummaryRepository.save(summary);
                        }
                );
    }

    private Long findBestSellingMenuId(List<Order> orders) {
        return orders.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getMenuId,
                        Collectors.summingInt(OrderItem::getQuantity)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
