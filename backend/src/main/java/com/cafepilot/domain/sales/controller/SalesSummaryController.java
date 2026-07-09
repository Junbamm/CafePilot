package com.cafepilot.domain.sales.controller;

import com.cafepilot.domain.sales.dto.SalesSummaryResponse;
import com.cafepilot.domain.sales.service.SalesSummaryService;
import com.cafepilot.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Sales", description = "판매 집계 API")
@RestController
@RequestMapping("/api/v1/cafes/{cafeId}/sales")
@RequiredArgsConstructor
public class SalesSummaryController {

    private final SalesSummaryService salesSummaryService;

    @Operation(summary = "기간별 판매 집계 조회")
    @GetMapping
    public ApiResponse<List<SalesSummaryResponse>> getSummaries(
            @PathVariable Long cafeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ApiResponse.success(salesSummaryService.getSummaries(cafeId, from, to));
    }
}
