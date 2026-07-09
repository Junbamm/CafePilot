package com.cafepilot.domain.cafe.controller;

import com.cafepilot.domain.cafe.dto.CafeResponse;
import com.cafepilot.domain.cafe.dto.CreateCafeRequest;
import com.cafepilot.domain.cafe.dto.UpdateCafeRequest;
import com.cafepilot.domain.cafe.service.CafeService;
import com.cafepilot.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cafe", description = "카페 API")
@RestController
@RequestMapping("/api/v1/cafes")
@RequiredArgsConstructor
public class CafeController {

    private final CafeService cafeService;

    @Operation(summary = "내 카페 조회")
    @GetMapping("/my")
    public ApiResponse<CafeResponse> getMyCafe(@AuthenticationPrincipal Long memberId) {
        return ApiResponse.success(cafeService.getMyCafe(memberId));
    }

    @Operation(summary = "카페 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CafeResponse> createCafe(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CreateCafeRequest request
    ) {
        return ApiResponse.success(cafeService.createCafe(memberId, request));
    }

    @Operation(summary = "카페 정보 수정")
    @PutMapping("/{cafeId}")
    public ApiResponse<CafeResponse> updateCafe(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId,
            @Valid @RequestBody UpdateCafeRequest request
    ) {
        return ApiResponse.success(cafeService.updateCafe(memberId, cafeId, request));
    }

    @Operation(summary = "영업 상태 토글 (오픈/마감)")
    @PatchMapping("/{cafeId}/open")
    public ApiResponse<CafeResponse> toggleOpen(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId
    ) {
        return ApiResponse.success(cafeService.toggleOpen(memberId, cafeId));
    }

    @Operation(summary = "카페 삭제")
    @DeleteMapping("/{cafeId}")
    public ApiResponse<Void> deleteCafe(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long cafeId
    ) {
        cafeService.deleteCafe(memberId, cafeId);
        return ApiResponse.success();
    }
}
